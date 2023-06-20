package com.tari.android.wallet.infrastructure.bluetooth

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid
import com.tari.android.wallet.application.deeplinks.DeepLink
import com.tari.android.wallet.application.deeplinks.DeeplinkHandler
import com.tari.android.wallet.data.sharedPrefs.SharedPrefsRepository
import com.tari.android.wallet.data.sharedPrefs.bluetooth.BluetoothServerState
import com.tari.android.wallet.data.sharedPrefs.bluetooth.ShareSettingsRepository
import com.tari.android.wallet.extension.addTo
import com.welie.blessed.BluetoothCentral
import com.welie.blessed.BluetoothPeripheralManager
import com.welie.blessed.BluetoothPeripheralManagerCallback
import com.welie.blessed.GattStatus
import com.welie.blessed.ReadResponse
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TariBluetoothServer @Inject constructor(
    private val shareSettingsRepository: ShareSettingsRepository,
    val deeplinkHandler: DeeplinkHandler,
    val sharedPrefsRepository: SharedPrefsRepository
) :
    TariBluetoothAdapter() {

    private var bluetoothGattServer: BluetoothGattServer? = null

    override fun onContextSet() {
        super.onContextSet()
        compositeDisposable.dispose()
        compositeDisposable = CompositeDisposable()
        stopReceiving()
        handleReceiving()
        shareSettingsRepository.updateNotifier.subscribe {
            try {
                handleReceiving()
            } catch (e: Throwable) {
                logger.e("Error handling receiving", e)
            }
        }.addTo(compositeDisposable)
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            logger.i("onStartSuccess: $settingsInEffect")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            logger.i("onStartFailure: $errorCode")
        }
    }

    private fun handleReceiving() {
        when (shareSettingsRepository.bluetoothSettingsState) {
            BluetoothServerState.DISABLED -> stopReceiving()
            BluetoothServerState.WHILE_UP -> startReceiving()
            BluetoothServerState.ENABLED -> startReceiving()
            null -> Unit
        }
    }

    private fun startReceiving() {
        ensureBluetoothIsEnabled {
            runWithPermissions(bluetoothAdvertisePermission) {
                runWithPermissions(bluetoothConnectPermission) {
                    doReceiving2()
                }
            }
        }
    }

    var onReceived: (List<DeepLink.Contacts.DeeplinkContact>) -> Unit = {}

    private fun doReceiving2() {
        val callback = object : BluetoothPeripheralManagerCallback() {

            var wholeData = byteArrayOf()

            var throttle: Disposable? = null

            override fun onCharacteristicWrite(
                bluetoothCentral: BluetoothCentral,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray?
            ): GattStatus {

                if (characteristic.uuid.toString().lowercase() == CHARACTERISTIC_UUID.lowercase()) {
                    wholeData += value!!
                }

                throttle?.dispose()
                throttle = io.reactivex.Observable.timer(1000, TimeUnit.MILLISECONDS)
                    .subscribe { doHandling(String(wholeData, Charsets.UTF_16)) }

                return GattStatus.SUCCESS
            }

            override fun onCharacteristicWriteCompleted(
                bluetoothCentral: BluetoothCentral,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray?
            ) {
                super.onCharacteristicWriteCompleted(bluetoothCentral, characteristic, value)

                throttle?.dispose()
                doHandling(String(wholeData, Charsets.UTF_16))
            }

            private fun doHandling(string: String): GattStatus {
                val handled = runCatching { deeplinkHandler.handle(string) }.getOrNull()

                if (handled != null && handled is DeepLink.Contacts) {
                    wholeData = byteArrayOf()
                    onReceived.invoke(handled.contacts)
                }
                return if (handled != null) GattStatus.SUCCESS else GattStatus.INVALID_HANDLE
            }

            override fun onCharacteristicRead(bluetoothCentral: BluetoothCentral, characteristic: BluetoothGattCharacteristic): ReadResponse {
                return if (characteristic.uuid.toString().lowercase() == TRANSACTION_DATA_UUID.lowercase()) {
                    val data = deeplinkHandler.getDeeplink(
                        DeepLink.UserProfile(
                            sharedPrefsRepository.publicKeyHexString.orEmpty(),
                            sharedPrefsRepository.name.orEmpty()
                        )
                    )
                    ReadResponse(GattStatus.SUCCESS, data.toByteArray(Charsets.UTF_8))
                } else super.onCharacteristicRead(bluetoothCentral, characteristic)
            }
        }

        val myService = BluetoothGattService(UUID.fromString(SERVICE_UUID), BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val myCharacteristic = BluetoothGattCharacteristic(
            UUID.fromString(CHARACTERISTIC_UUID),
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        val myProfileCharacteristic = BluetoothGattCharacteristic(
            UUID.fromString(TRANSACTION_DATA_UUID),
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        myService.addCharacteristic(myCharacteristic)
        myService.addCharacteristic(myProfileCharacteristic)

        val manager = BluetoothPeripheralManager(fragappCompatActivity!!, bluetoothManager!!, callback)
        manager.add(myService)

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(UUID.fromString(SERVICE_UUID)))
            .build()
        manager.startAdvertising(settings, data, data)
    }

    private fun stopReceiving() {
        bluetoothAdapter ?: return
        val bluetoothLeAdvertiser = bluetoothAdapter!!.bluetoothLeAdvertiser

        @Suppress("MissingPermission")
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)

        bluetoothGattServer?.let {
            @Suppress("MissingPermission")
            it.clearServices()
            @Suppress("MissingPermission")
            it.close()
        }
        bluetoothGattServer = null
    }
}