package com.tari.android.wallet.ui.fragment.send.addAmount

import androidx.lifecycle.SavedStateHandle
import com.tari.android.wallet.R
import com.tari.android.wallet.event.EffectChannelFlow
import com.tari.android.wallet.extension.getOrNull
import com.tari.android.wallet.extension.getWithError
import com.tari.android.wallet.extension.launchOnIo
import com.tari.android.wallet.ffi.runWithDestroy
import com.tari.android.wallet.model.BalanceInfo
import com.tari.android.wallet.model.MicroTari
import com.tari.android.wallet.model.TariWalletAddress
import com.tari.android.wallet.model.WalletError
import com.tari.android.wallet.navigation.Navigation
import com.tari.android.wallet.navigation.TariNavigator.Companion.PARAMETER_AMOUNT
import com.tari.android.wallet.navigation.TariNavigator.Companion.PARAMETER_CONTACT
import com.tari.android.wallet.navigation.TariNavigator.Companion.PARAMETER_NOTE
import com.tari.android.wallet.network.NetworkConnectionStateHandler
import com.tari.android.wallet.ui.common.CommonViewModel
import com.tari.android.wallet.ui.dialog.modular.modules.body.BodyModule
import com.tari.android.wallet.ui.dialog.modular.modules.button.ButtonModule
import com.tari.android.wallet.ui.dialog.modular.modules.button.ButtonStyle
import com.tari.android.wallet.ui.dialog.modular.modules.head.HeadModule
import com.tari.android.wallet.ui.fragment.contactBook.data.contacts.ContactDto
import com.tari.android.wallet.ui.fragment.send.addAmount.feeModule.FeeModule
import com.tari.android.wallet.ui.fragment.send.addAmount.feeModule.NetworkSpeed
import com.tari.android.wallet.ui.fragment.send.common.TransactionData
import com.tari.android.wallet.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class AddAmountViewModel(savedState: SavedStateHandle) : CommonViewModel() {

    @Inject
    lateinit var networkConnection: NetworkConnectionStateHandler

    var selectedFeeData: FeeData? = null
    private var selectedSpeed: NetworkSpeed = NetworkSpeed.Medium

    private var feeData: List<FeeData> = listOf()

    init {
        component.inject(this)
    }

    private val _uiState = MutableStateFlow(
        savedState.get<ContactDto>(PARAMETER_CONTACT)?.let { contact ->
            AddAmountModel.UiState(
                isOneSidedPaymentEnabled = tariSettingsSharedRepository.isOneSidePaymentEnabled,
                isOneSidedPaymentForced = contact.contactInfo.requireWalletAddress().oneSided && !contact.contactInfo.requireWalletAddress().interactive,
                amount = savedState.get<MicroTari>(PARAMETER_AMOUNT)?.tariValue?.toDouble() ?: Double.MIN_VALUE,
                contactDto = contact,
                note = savedState.get<String>(PARAMETER_NOTE).orEmpty(),
            )
        } ?: error("Contact is required, but not provided")
    )
    val uiState = _uiState.asStateFlow()

    private val _effect = EffectChannelFlow<AddAmountModel.Effect>()
    val effect = _effect.flow

    val walletBalance: BalanceInfo
        get() = walletManager.requireWalletInstance.getBalance()

    init {
        doOnWalletServiceConnected { _effect.send(AddAmountModel.Effect.OnServiceConnected(uiState.value)) }

        loadFees()
    }

    private fun loadFees() = doOnWalletServiceConnected {
        launchOnIo {
            try {
                _uiState.update {
                    it.copy(
                        feePerGrams = walletManager.requireWalletInstance.getFeePerGramStats()
                            .runWithDestroy { ffiGrams -> FeePerGramOptions(ffiGrams) },
                    )
                }
            } catch (e: Throwable) {
                logger.i("Error loading fees: ${e.message}")
            }
        }
    }

    fun toggleOneSidePayment() {
        val newValue = !tariSettingsSharedRepository.isOneSidePaymentEnabled
        tariSettingsSharedRepository.isOneSidePaymentEnabled = newValue
    }

    fun showFeeDialog() {
        val feeModule = FeeModule(MicroTari(), feeData, selectedSpeed)
        showModularDialog(
            HeadModule(resourceManager.getString(R.string.add_amount_modify_fee_title)),
            BodyModule(resourceManager.getString(R.string.add_amount_modify_fee_description)),
            feeModule,
            ButtonModule(resourceManager.getString(R.string.add_amount_modify_fee_use), ButtonStyle.Normal) {
                selectedSpeed = feeModule.selectedSpeed
                selectedFeeData = feeModule.feePerGram
                hideDialog()
            },
            ButtonModule(resourceManager.getString(R.string.common_cancel), ButtonStyle.Close)
        )
    }

    fun emojiIdClicked(walletAddress: TariWalletAddress) {
        showAddressDetailsDialog(walletAddress)
    }

    fun calculateFee(amount: MicroTari) {
        try {
            val grams = uiState.value.feePerGrams
            if (grams == null) {
                calculateDefaultFees(amount)
                return
            }

            val wallet = walletManager.requireWalletInstance

            val slowFee = wallet.getWithError(this::showFeeError) { it.estimateTxFee(amount, grams.slow) }
            val mediumFee = wallet.getWithError(this::showFeeError) { it.estimateTxFee(amount, grams.medium) }
            val fastFee = wallet.getWithError(this::showFeeError) { it.estimateTxFee(amount, grams.fast) }

            if (slowFee == null || mediumFee == null || fastFee == null) {
                calculateDefaultFees(amount)
                return
            }

            feeData = listOf(FeeData(grams.slow, slowFee), FeeData(grams.medium, mediumFee), FeeData(grams.fast, fastFee))
            selectedFeeData = feeData[1]
        } catch (e: Throwable) {
            logger.i(e.message + "calculate fees")
        }
    }

    fun continueToAddNote(transactionData: TransactionData) {
        if (!networkConnection.isNetworkConnected()) {
            showInternetConnectionErrorDialog()
        } else if (transactionData.note.isNullOrEmpty()) {
            tariNavigator.navigate(Navigation.AddAmount.ContinueToAddNote(transactionData))
        } else {
            tariNavigator.navigate(Navigation.AddAmount.ContinueToFinalizing(transactionData))
        }
    }

    fun showAmountExceededError() {
        showSimpleDialog(
            title = resourceManager.getString(R.string.error_balance_exceeded_title),
            description = resourceManager.getString(R.string.error_balance_exceeded_description),
        )
    }

    private fun calculateDefaultFees(amount: MicroTari) {
        val calculatedFee = walletManager.requireWalletInstance.getOrNull { wallet ->
            wallet.estimateTxFee(amount, Constants.Wallet.DEFAULT_FEE_PER_GRAM)
        } ?: return
        selectedFeeData = FeeData(Constants.Wallet.DEFAULT_FEE_PER_GRAM, calculatedFee)
    }

    private fun showFeeError(walletError: WalletError) {
        if (walletError != WalletError.NoError) {
            showFeeError()
        }
    }

    private fun showFeeError() = Unit
}