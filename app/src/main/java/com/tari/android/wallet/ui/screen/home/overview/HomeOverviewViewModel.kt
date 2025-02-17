package com.tari.android.wallet.ui.screen.home.overview


import android.app.Activity
import android.os.Build
import com.tari.android.wallet.R
import com.tari.android.wallet.R.string.error_no_connection_description
import com.tari.android.wallet.R.string.error_no_connection_title
import com.tari.android.wallet.R.string.error_node_unreachable_description
import com.tari.android.wallet.R.string.error_node_unreachable_title
import com.tari.android.wallet.application.deeplinks.DeepLink
import com.tari.android.wallet.application.deeplinks.DeeplinkManager
import com.tari.android.wallet.application.securityStage.StagedWalletSecurityManager
import com.tari.android.wallet.application.walletManager.WalletManager.WalletEvent
import com.tari.android.wallet.data.BalanceStateHandler
import com.tari.android.wallet.data.sharedPrefs.CorePrefRepository
import com.tari.android.wallet.data.sharedPrefs.sentry.SentryPrefRepository
import com.tari.android.wallet.data.tx.TxRepository
import com.tari.android.wallet.util.extension.collectFlow
import com.tari.android.wallet.util.extension.launchOnIo
import com.tari.android.wallet.model.tx.Tx
import com.tari.android.wallet.navigation.Navigation
import com.tari.android.wallet.ui.common.CommonViewModel
import com.tari.android.wallet.ui.common.giphy.presentation.GifViewModel
import com.tari.android.wallet.ui.common.giphy.repository.GifRepository
import com.tari.android.wallet.ui.dialog.modular.DialogArgs
import com.tari.android.wallet.ui.dialog.modular.ModularDialogArgs
import com.tari.android.wallet.ui.dialog.modular.modules.body.BodyModule
import com.tari.android.wallet.ui.dialog.modular.modules.button.ButtonModule
import com.tari.android.wallet.ui.dialog.modular.modules.button.ButtonStyle
import com.tari.android.wallet.ui.dialog.modular.modules.head.HeadModule
import com.tari.android.wallet.data.contacts.ContactsRepository
import com.tari.android.wallet.ui.screen.send.finalize.FinalizeSendTxModel.TxFailureReason
import com.tari.android.wallet.ui.screen.tx.adapter.TxViewHolderItem
import com.tari.android.wallet.util.extractEmojis
import com.tari.android.wallet.util.shortString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


class HomeOverviewViewModel : CommonViewModel() {

    @Inject
    lateinit var corePrefRepository: CorePrefRepository

    @Inject
    lateinit var contactsRepository: ContactsRepository

    @Inject
    lateinit var transactionRepository: TxRepository

    @Inject
    lateinit var sentryPrefRepository: SentryPrefRepository

    @Inject
    lateinit var stagedWalletSecurityManager: StagedWalletSecurityManager

    @Inject
    lateinit var deeplinkManager: DeeplinkManager

    @Inject
    lateinit var balanceStateHandler: BalanceStateHandler

    @Inject
    lateinit var gifRepository: GifRepository

    init {
        component.inject(this)
    }

    private val stagedSecurityDelegate = StagedSecurityDelegate(
        dialogHandler = this,
        stagedWalletSecurityManager = stagedWalletSecurityManager,
        resourceManager = resourceManager,
        tariNavigator = tariNavigator,
    )

    private val _uiState = MutableStateFlow(
        HomeOverviewModel.UiState(
            avatarEmoji = corePrefRepository.walletAddress.coreKeyEmojis.extractEmojis().take(1).joinToString(""),
            emojiMedium = corePrefRepository.walletAddress.shortString(),
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        component.inject(this)

        collectFlow(balanceStateHandler.balanceState) { balanceInfo ->
            _uiState.update { it.copy(balance = balanceInfo) }
            stagedSecurityDelegate.handleStagedSecurity(balanceInfo)
        }

        collectFlow(transactionRepository.allTxs) { txs ->
            _uiState.update { state ->
                state.copy(
                    txList = txs.sortedByDescending { it.tx.timestamp }
                        .map { txDto ->
                            TxViewHolderItem(
                                txDto = txDto,
                                gifViewModel = GifViewModel(gifRepository),
                            )
                        }
                        .take(TRANSACTION_AMOUNT_HOME_PAGE),
                )
            }
        }

        collectFlow(walletManager.walletEvent) { event ->
            when (event) {
                is WalletEvent.TxSend.TxSendFailed -> onTxSendFailed(event.failureReason)

                else -> Unit
            }
        }

        checkForDataConsent()

        showRecoverySuccessIfNeeded()
    }

    fun navigateToTxList(tx: Tx) {
        tariNavigator.navigate(Navigation.TxList.ToTxDetails(tx))
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionManager.runWithPermission(listOf(android.Manifest.permission.POST_NOTIFICATIONS)) {
                logger.i("notification permission checked successfully")
            }
        }

        grantContactsPermission()
    }

    fun grantContactsPermission() {
        permissionManager.runWithPermission(
            permissions = listOf(
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.WRITE_CONTACTS,
            ),
            silently = true,
        ) {
            launchOnIo {
                contactsRepository.grantContactPermissionAndRefresh()
            }
        }
    }

    fun handleDeeplink(context: Activity, deepLink: DeepLink) {
        deeplinkManager.execute(context, deepLink)
    }

    private fun checkForDataConsent() {
        if (sentryPrefRepository.isEnabled == null) {
            sentryPrefRepository.isEnabled = false
            showModularDialog(
                ModularDialogArgs(DialogArgs(cancelable = false, canceledOnTouchOutside = false), listOf(
                    HeadModule(resourceManager.getString(R.string.data_collection_dialog_title)),
                    BodyModule(resourceManager.getString(R.string.data_collection_dialog_description)),
                    ButtonModule(resourceManager.getString(R.string.data_collection_dialog_positive), ButtonStyle.Normal) {
                        sentryPrefRepository.isEnabled = true
                        hideDialog()
                    },
                    ButtonModule(resourceManager.getString(R.string.data_collection_dialog_negative), ButtonStyle.Close) {
                        sentryPrefRepository.isEnabled = false
                        hideDialog()
                    }
                ))
            )
        }
    }

    private fun showRecoverySuccessIfNeeded() {
        if (corePrefRepository.needToShowRecoverySuccessDialog) {
            showSimpleDialog(
                titleRes = R.string.recovery_success_dialog_title,
                descriptionRes = R.string.recovery_success_dialog_description,
                closeButtonTextRes = R.string.recovery_success_dialog_close,
                onClose = { corePrefRepository.needToShowRecoverySuccessDialog = false },
            )
        }
    }

    private fun onTxSendFailed(failureReason: TxFailureReason) = when (failureReason) {
        TxFailureReason.NETWORK_CONNECTION_ERROR -> displayNetworkConnectionErrorDialog()
        TxFailureReason.BASE_NODE_CONNECTION_ERROR, TxFailureReason.SEND_ERROR -> displayBaseNodeConnectionErrorDialog()
    }

    private fun displayNetworkConnectionErrorDialog() {
        showSimpleDialog(
            title = resourceManager.getString(error_no_connection_title),
            description = resourceManager.getString(error_no_connection_description),
        )
    }

    private fun displayBaseNodeConnectionErrorDialog() {
        showSimpleDialog(
            title = resourceManager.getString(error_node_unreachable_title),
            description = resourceManager.getString(error_node_unreachable_description),
        )
    }

    companion object {
        private const val TRANSACTION_AMOUNT_HOME_PAGE = 2
    }
}