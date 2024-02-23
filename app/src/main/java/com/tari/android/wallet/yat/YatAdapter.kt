package com.tari.android.wallet.yat

import android.app.Activity
import android.app.ActivityOptions
import android.app.Application
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import com.tari.android.wallet.BuildConfig
import com.tari.android.wallet.data.sharedPrefs.SharedPrefsRepository
import com.tari.android.wallet.data.sharedPrefs.network.NetworkRepository
import com.tari.android.wallet.ui.fragment.send.common.TransactionData
import com.tari.android.wallet.ui.fragment.send.finalize.FinalizeSendTxViewModel
import com.tari.android.wallet.ui.fragment.send.finalize.YatFinalizeSendTxActivity
import com.tari.android.wallet.util.DebugConfig
import yat.android.data.YatRecord
import yat.android.data.YatRecordType
import yat.android.lib.YatConfiguration
import yat.android.lib.YatIntegration
import yat.android.lib.YatLibApi
import yat.android.sdk.models.PaymentAddressResponse
import yat.android.ui.transactions.outcoming.YatLibOutcomingTransactionData
import java.io.Serializable

class YatAdapter(
    private val yatSharedRepository: YatSharedRepository,
    private val networkRepository: NetworkRepository,
    private val commonRepository: SharedPrefsRepository
) : YatIntegration.Delegate {

    private val logger
        get() = Logger.t(YatAdapter::class.simpleName)

    fun initYat(application: Application) {
        val config = YatConfiguration(
            appReturnLink = BuildConfig.YAT_ORGANIZATION_RETURN_URL,
            organizationName = BuildConfig.YAT_ORGANIZATION_NAME,
            organizationKey = BuildConfig.YAT_ORGANIZATION_KEY,
        )
        YatIntegration.setup(
            context = application,
            config = config,
            colorMode = YatIntegration.ColorMode.LIGHT,
            delegate = this,
            environment = DebugConfig.yatEnvironment,
        )
    }

    fun searchTariYats(query: String): PaymentAddressResponse? =
        kotlin.runCatching { YatLibApi.emojiIDApi.lookupEmojiIDPayment(query, "0x0103") }.getOrNull()

    fun searchAnyYats(query: String): PaymentAddressResponse? =
        kotlin.runCatching { YatLibApi.emojiIDApi.lookupEmojiIDPayment(query, null) }.getOrNull()

    fun openOnboarding(context: Context) {
        val address = commonRepository.publicKeyHexString.orEmpty()
        YatIntegration.showOnboarding(context, listOf(YatRecord(YatRecordType.XTR_ADDRESS, data = address)))
    }

    fun showOutcomingFinalizeActivity(activity: Activity, transactionData: TransactionData) {
        val yatUser = transactionData.recipientContact?.getYatDto() ?: return
        val currentTicker = networkRepository.currentNetwork?.ticker.orEmpty()
        val data = YatLibOutcomingTransactionData(transactionData.amount!!.tariValue.toDouble(), currentTicker, yatUser.yat)

        val intent = Intent(activity, YatFinalizeSendTxActivity::class.java)
        intent.putExtra("YatLibDataKey", data as Serializable)
        val gson = Gson().toJson(transactionData)
        intent.putExtra(FinalizeSendTxViewModel.transactionDataKey, gson)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        activity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle())
    }

    override fun onYatIntegrationComplete(yat: String) {
        logger.d("Yat integration completed with $yat")
        yatSharedRepository.saveYat(yat)
    }

    override fun onYatIntegrationFailed(failureType: YatIntegration.FailureType) {
        logger.d("Yat integration failed $failureType")
    }
}