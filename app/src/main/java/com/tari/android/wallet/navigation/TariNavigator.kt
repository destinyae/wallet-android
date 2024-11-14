package com.tari.android.wallet.navigation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import com.tari.android.wallet.R
import com.tari.android.wallet.application.YatAdapter
import com.tari.android.wallet.application.YatAdapter.ConnectedWallet
import com.tari.android.wallet.application.deeplinks.DeepLink
import com.tari.android.wallet.application.walletManager.WalletManager
import com.tari.android.wallet.model.MicroTari
import com.tari.android.wallet.model.TariWalletAddress
import com.tari.android.wallet.model.Tx
import com.tari.android.wallet.model.TxId
import com.tari.android.wallet.navigation.Navigation.AddAmountNavigation
import com.tari.android.wallet.navigation.Navigation.AllSettingsNavigation
import com.tari.android.wallet.navigation.Navigation.BackupSettingsNavigation
import com.tari.android.wallet.navigation.Navigation.ChatNavigation
import com.tari.android.wallet.navigation.Navigation.ChooseRestoreOptionNavigation
import com.tari.android.wallet.navigation.Navigation.ContactBookNavigation
import com.tari.android.wallet.navigation.Navigation.CustomBridgeNavigation
import com.tari.android.wallet.navigation.Navigation.InputSeedWordsNavigation
import com.tari.android.wallet.navigation.Navigation.SendTxNavigation
import com.tari.android.wallet.navigation.Navigation.TorBridgeNavigation
import com.tari.android.wallet.navigation.Navigation.TxListNavigation
import com.tari.android.wallet.navigation.Navigation.VerifySeedPhraseNavigation
import com.tari.android.wallet.network.NetworkConnectionStateHandler
import com.tari.android.wallet.ui.common.CommonActivity
import com.tari.android.wallet.ui.common.CommonFragment
import com.tari.android.wallet.ui.dialog.modular.DialogArgs
import com.tari.android.wallet.ui.dialog.modular.ModularDialog
import com.tari.android.wallet.ui.dialog.modular.ModularDialogArgs
import com.tari.android.wallet.ui.dialog.modular.modules.body.BodyModule
import com.tari.android.wallet.ui.dialog.modular.modules.button.ButtonModule
import com.tari.android.wallet.ui.dialog.modular.modules.button.ButtonStyle
import com.tari.android.wallet.ui.dialog.modular.modules.head.HeadModule
import com.tari.android.wallet.ui.extension.parcelable
import com.tari.android.wallet.ui.extension.showInternetConnectionErrorDialog
import com.tari.android.wallet.ui.extension.string
import com.tari.android.wallet.ui.fragment.auth.FeatureAuthFragment
import com.tari.android.wallet.ui.fragment.biometrics.ChangeBiometricsFragment
import com.tari.android.wallet.ui.fragment.chat.addChat.AddChatFragment
import com.tari.android.wallet.ui.fragment.chat.chatDetails.ChatDetailsFragment
import com.tari.android.wallet.ui.fragment.contactBook.add.AddContactFragment
import com.tari.android.wallet.ui.fragment.contactBook.add.SelectUserContactFragment
import com.tari.android.wallet.ui.fragment.contactBook.data.contacts.ContactDto
import com.tari.android.wallet.ui.fragment.contactBook.details.ContactDetailsFragment
import com.tari.android.wallet.ui.fragment.contactBook.link.ContactLinkFragment
import com.tari.android.wallet.ui.fragment.contactBook.root.ContactBookFragment
import com.tari.android.wallet.ui.fragment.home.HomeActivity
import com.tari.android.wallet.ui.fragment.home.overview.HomeOverviewFragment
import com.tari.android.wallet.ui.fragment.onboarding.activity.OnboardingFlowActivity
import com.tari.android.wallet.ui.fragment.onboarding.localAuth.LocalAuthFragment
import com.tari.android.wallet.ui.fragment.pinCode.EnterPinCodeFragment
import com.tari.android.wallet.ui.fragment.profile.WalletInfoFragment
import com.tari.android.wallet.ui.fragment.restore.enterRestorationPassword.EnterRestorationPasswordFragment
import com.tari.android.wallet.ui.fragment.restore.inputSeedWords.InputSeedWordsFragment
import com.tari.android.wallet.ui.fragment.restore.walletRestoring.WalletRestoringFragment
import com.tari.android.wallet.ui.fragment.send.addAmount.AddAmountFragment
import com.tari.android.wallet.ui.fragment.send.addNote.AddNoteFragment
import com.tari.android.wallet.ui.fragment.send.common.TransactionData
import com.tari.android.wallet.ui.fragment.send.finalize.FinalizeSendTxFragment
import com.tari.android.wallet.ui.fragment.send.requestTari.RequestTariFragment
import com.tari.android.wallet.ui.fragment.send.transfer.TransferFragment
import com.tari.android.wallet.ui.fragment.settings.allSettings.AllSettingsFragment
import com.tari.android.wallet.ui.fragment.settings.allSettings.about.TariAboutFragment
import com.tari.android.wallet.ui.fragment.settings.backgroundService.BackgroundServiceSettingsFragment
import com.tari.android.wallet.ui.fragment.settings.backup.backupOnboarding.BackupOnboardingFlowFragment
import com.tari.android.wallet.ui.fragment.settings.backup.backupSettings.BackupSettingsFragment
import com.tari.android.wallet.ui.fragment.settings.backup.changeSecurePassword.ChangeSecurePasswordFragment
import com.tari.android.wallet.ui.fragment.settings.backup.enterCurrentPassword.EnterCurrentPasswordFragment
import com.tari.android.wallet.ui.fragment.settings.backup.verifySeedPhrase.VerifySeedPhraseFragment
import com.tari.android.wallet.ui.fragment.settings.backup.writeDownSeedWords.WriteDownSeedPhraseFragment
import com.tari.android.wallet.ui.fragment.settings.baseNodeConfig.changeBaseNode.ChangeBaseNodeFragment
import com.tari.android.wallet.ui.fragment.settings.bluetoothSettings.BluetoothSettingsFragment
import com.tari.android.wallet.ui.fragment.settings.dataCollection.DataCollectionFragment
import com.tari.android.wallet.ui.fragment.settings.deleteWallet.DeleteWalletFragment
import com.tari.android.wallet.ui.fragment.settings.logs.activity.DebugActivity
import com.tari.android.wallet.ui.fragment.settings.logs.activity.DebugNavigation
import com.tari.android.wallet.ui.fragment.settings.networkSelection.NetworkSelectionFragment
import com.tari.android.wallet.ui.fragment.settings.screenRecording.ScreenRecordingSettingsFragment
import com.tari.android.wallet.ui.fragment.settings.themeSelector.ThemeSelectorFragment
import com.tari.android.wallet.ui.fragment.settings.torBridges.TorBridgesSelectionFragment
import com.tari.android.wallet.ui.fragment.settings.torBridges.customBridges.CustomTorBridgesFragment
import com.tari.android.wallet.ui.fragment.tx.details.TxDetailsFragment
import com.tari.android.wallet.ui.fragment.tx.history.all.AllTxHistoryFragment
import com.tari.android.wallet.ui.fragment.tx.history.contact.ContactTxHistoryFragment
import com.tari.android.wallet.ui.fragment.utxos.list.UtxosListFragment
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

// TODO: move navigation logic to only the navigate() method and make all navigation methods private
@Singleton
class TariNavigator @Inject constructor(
    private val walletManager: WalletManager,
    private val yatAdapter: YatAdapter,
    private val networkConnection: NetworkConnectionStateHandler,
) {

    lateinit var activity: CommonActivity<*, *>

    fun navigate(navigation: Navigation) {
        when (navigation) {
            is Navigation.EnterPinCodeNavigation -> addFragment(EnterPinCodeFragment.newInstance(navigation.behavior, navigation.stashedPin))
            is Navigation.ChangeBiometrics -> addFragment(ChangeBiometricsFragment())
            is Navigation.FeatureAuth -> addFragment(FeatureAuthFragment())
            is Navigation.SplashScreen -> toSplash(navigation.seedWords, navigation.clearTop)
            is ContactBookNavigation.ToAddContact -> toAddContact()
            is ContactBookNavigation.ToContactDetails -> toContactDetails(navigation.contact)
            is ContactBookNavigation.ToRequestTari -> toRequestTariFromContact(navigation.contact)
            is ContactBookNavigation.ToSendTari -> toSendTariToContact(navigation.contact)
            is ContactBookNavigation.ToLinkContact -> toLinkContact(navigation.contact)
            is ContactBookNavigation.BackToContactBook -> backToContactBook()
            is ContactBookNavigation.ToExternalWallet -> toExternalWallet(navigation.connectedWallet)
            is ContactBookNavigation.ToContactTransactionHistory -> addFragment(ContactTxHistoryFragment.createFragment(navigation.contact))
            is ContactBookNavigation.ToAddPhoneContact -> toAddPhoneContact()
            is ContactBookNavigation.ToSelectTariUser -> addFragment(SelectUserContactFragment.newInstance())
            is ChooseRestoreOptionNavigation.ToEnterRestorePassword -> toEnterRestorePassword()
            is ChooseRestoreOptionNavigation.ToRestoreWithRecoveryPhrase -> addFragment(InputSeedWordsFragment.createFragment())
            is AllSettingsNavigation.ToBugReporting -> DebugActivity.launch(activity, DebugNavigation.BugReport)
            is AllSettingsNavigation.ToMyProfile -> toMyProfile()
            is AllSettingsNavigation.ToAbout -> toAbout()
            is AllSettingsNavigation.ToBackgroundService -> toBackgroundService()
            is AllSettingsNavigation.ToScreenRecording -> toScreenRecording()
            is AllSettingsNavigation.ToBluetoothSettings -> addFragment(BluetoothSettingsFragment())
            is AllSettingsNavigation.ToBackupSettings -> toBackupSettings(true)
            is AllSettingsNavigation.ToBaseNodeSelection -> toBaseNodeSelection()
            is AllSettingsNavigation.ToDeleteWallet -> toDeleteWallet()
            is AllSettingsNavigation.ToNetworkSelection -> toNetworkSelection()
            is AllSettingsNavigation.ToTorBridges -> toTorBridges()
            is AllSettingsNavigation.ToDataCollection -> addFragment(DataCollectionFragment())
            is AllSettingsNavigation.ToThemeSelection -> toThemeSelection()
            is AllSettingsNavigation.ToRequestTari -> addFragment(RequestTariFragment.newInstance())
            is InputSeedWordsNavigation.ToRestoreFromSeeds -> addFragment(WalletRestoringFragment.newInstance())
            is InputSeedWordsNavigation.ToBaseNodeSelection -> toBaseNodeSelection()
            is AddAmountNavigation.OnAmountExceedsActualAvailableBalance -> onAmountExceedsActualAvailableBalance()
            is AddAmountNavigation.ContinueToAddNote -> continueToAddNote(navigation.transactionData)
            is AddAmountNavigation.ContinueToFinalizing -> continueToFinalizeSendTx(navigation.transactionData)
            is TxListNavigation.ToChat -> toChat()
            is TxListNavigation.ToTxDetails -> toTxDetails(tx = navigation.tx)
            is TxListNavigation.ToSendTariToUser -> toSendTari(navigation.contact, navigation.amount)
            is TxListNavigation.ToSendWithDeeplink -> toSendWithDeeplink(navigation.sendDeeplink)
            is TxListNavigation.ToUtxos -> toUtxos()
            is TxListNavigation.ToAllSettings -> toAllSettings()
            is TxListNavigation.ToTransfer -> addFragment(TransferFragment())
            is TxListNavigation.HomeTransactionHistory -> addFragment(AllTxHistoryFragment())
            is SendTxNavigation.OnSendTxFailure -> navigateBackFromTxSend(navigation.isYat)
            is SendTxNavigation.OnSendTxSuccess -> navigateBackFromTxSend(navigation.isYat)
            is TorBridgeNavigation.ToCustomBridges -> toCustomTorBridges()
            is VerifySeedPhraseNavigation.ToSeedPhraseVerificationComplete -> onSeedPhraseVerificationComplete()
            is VerifySeedPhraseNavigation.ToSeedPhraseVerification -> toSeedPhraseVerification(navigation.seedWords)
            is BackupSettingsNavigation.ToChangePassword -> toChangePassword()
            is BackupSettingsNavigation.ToConfirmPassword -> toConfirmPassword()
            is BackupSettingsNavigation.ToWalletBackupWithRecoveryPhrase -> toWalletBackupWithRecoveryPhrase()
            is BackupSettingsNavigation.ToLearnMore -> toBackupOnboardingFlow()
            is CustomBridgeNavigation.UploadQrCode -> Unit
            is ChatNavigation.ToChat -> toChatDetail(navigation.walletAddress, navigation.isNew)
            is ChatNavigation.ToAddChat -> addFragment(AddChatFragment())
        }
    }

    private fun toSplash(seedWords: List<String>? = null, clearTop: Boolean = true) {
        activity.startActivity(Intent(activity, OnboardingFlowActivity::class.java).apply {
            flags = if (clearTop) Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            else Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(OnboardingFlowActivity.ARG_SEED_WORDS, seedWords?.toTypedArray())
        })
        activity.finishAffinity()
    }

    private fun toAddPhoneContact() {
        val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
        activity.startActivity(intent)
    }

    private fun toEnterRestorePassword() = addFragment(EnterRestorationPasswordFragment.newInstance())

    fun onBackPressed() = activity.onBackPressed()

    fun toTxDetails(tx: Tx? = null, txId: TxId? = null) = activity.addFragment(TxDetailsFragment().apply {
        arguments = Bundle().apply {
            putParcelable(TxDetailsFragment.TX_EXTRA_KEY, tx)
            putParcelable(TxDetailsFragment.TX_ID_EXTRA_KEY, txId)
        }
    })

    private fun toChat() = (activity as HomeActivity).ui.viewPager.setCurrentItem(INDEX_CHAT, NO_SMOOTH_SCROLL)

    fun toAllSettings() = (activity as HomeActivity).ui.viewPager.setCurrentItem(INDEX_SETTINGS, NO_SMOOTH_SCROLL)

    fun toBackupSettings(withAnimation: Boolean) = addFragment(BackupSettingsFragment.newInstance(), withAnimation = withAnimation)

    private fun toDeleteWallet() = addFragment(DeleteWalletFragment())

    private fun toBackgroundService() = addFragment(BackgroundServiceSettingsFragment())

    private fun toScreenRecording() = addFragment(ScreenRecordingSettingsFragment())

    private fun toMyProfile() = addFragment(WalletInfoFragment())

    private fun toAbout() = addFragment(TariAboutFragment())

    private fun toBackupOnboardingFlow() = addFragment(BackupOnboardingFlowFragment())

    private fun toBaseNodeSelection() = addFragment(ChangeBaseNodeFragment())

    private fun toTorBridges() = addFragment(TorBridgesSelectionFragment())

    private fun toThemeSelection() = addFragment(ThemeSelectorFragment())

    private fun toUtxos() = addFragment(UtxosListFragment())

    private fun toCustomTorBridges() = addFragment(CustomTorBridgesFragment())

    private fun toNetworkSelection() = addFragment(NetworkSelectionFragment())

    fun toWalletBackupWithRecoveryPhrase() = addFragment(WriteDownSeedPhraseFragment())

    private fun toSeedPhraseVerification(seedWords: List<String>) = addFragment(VerifySeedPhraseFragment.newInstance(seedWords))

    private fun toConfirmPassword() = addFragment(EnterCurrentPasswordFragment())

    fun toChangePassword() = addFragment(ChangeSecurePasswordFragment())

    private fun toSendTari(user: ContactDto, amount: MicroTari?) = sendToUser(user, amount)

    private fun toSendWithDeeplink(deeplink: DeepLink.Send) {
        popUpTo(HomeOverviewFragment::class.java.simpleName)
        sendToUserByDeeplink(deeplink)
    }

    private fun toAddContact() = addFragment(AddContactFragment())

    private fun toContactDetails(contact: ContactDto) = addFragment(ContactDetailsFragment.createFragment(contact))

    private fun toRequestTariFromContact(contact: ContactDto) = sendToUser(contact)

    private fun toSendTariToContact(contact: ContactDto) = sendToUser(contact)

    private fun backToContactBook() = popUpTo(ContactBookFragment::class.java.simpleName)

    fun backToAllSettings() = popUpTo(AllSettingsFragment::class.java.simpleName)

    fun backAfterAuth() {
        if (activity is HomeActivity) {
            popUpTo(AllSettingsFragment::class.java.simpleName)
        } else {
            popUpTo(LocalAuthFragment::class.java.simpleName)
        }
    }

    private fun toLinkContact(contact: ContactDto) = addFragment(ContactLinkFragment.createFragment(contact))

    private fun toExternalWallet(connectedWallet: ConnectedWallet) {
        try {
            val externalAddress = connectedWallet.getExternalLink()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(externalAddress))

            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(intent)
            } else {
                activity.viewModel.openWalletErrorDialog()
            }
        } catch (e: Throwable) {
            activity.viewModel.openWalletErrorDialog()
        }
    }

    private fun onAmountExceedsActualAvailableBalance() {
        val args = ModularDialogArgs(
            DialogArgs(), listOf(
                HeadModule(activity.string(R.string.error_balance_exceeded_title)),
                BodyModule(activity.string(R.string.error_balance_exceeded_description)),
                ButtonModule(activity.string(R.string.common_close), ButtonStyle.Close),
            )
        )
        ModularDialog(activity, args).show()
    }

    private fun continueToAddNote(transactionData: TransactionData) {
        if (!networkConnection.isNetworkConnected()) {
            showInternetConnectionErrorDialog(this.activity)
            return
        }
        addFragment(AddNoteFragment.newInstance(transactionData))
    }

    private fun continueToFinalizeSendTx(transactionData: TransactionData) {
        if (transactionData.recipientContact?.yat != null) {
            yatAdapter.showOutcomingFinalizeActivity(this.activity, transactionData)
        } else {
            addFragment(FinalizeSendTxFragment.create(transactionData))
        }
    }

    private fun navigateBackFromTxSend(isYat: Boolean) {
        val fragmentsCount = activity.supportFragmentManager.fragments.size - 5
        for (i in 0 until fragmentsCount) {
            activity.supportFragmentManager.popBackStackImmediate()
        }
    }

    fun onPasswordChanged() {
        popUpTo(BackupSettingsFragment::class.java.simpleName)
    }

    private fun onSeedPhraseVerificationComplete() {
        popUpTo(BackupSettingsFragment::class.java.simpleName)
    }

    private fun sendToUserByDeeplink(deeplink: DeepLink.Send) {
        walletManager.walletInstance?.getWalletAddress() // TODO move all the logic beside of navigation from here
        val address = TariWalletAddress.fromBase58(deeplink.walletAddress)
        val contact = (activity as HomeActivity).viewModel.contactsRepository.getContactByAddress(address)

        addFragment(AddAmountFragment.newInstance(contact, deeplink.amount, deeplink.note))
    }

    private fun sendToUser(recipientUser: ContactDto, amount: MicroTari? = null) {
        val contact = activity.intent.parcelable<ContactDto>(PARAMETER_CONTACT) ?: recipientUser
        val innerAmount = activity.intent.getDoubleExtra(PARAMETER_AMOUNT, Double.MIN_VALUE)
        val tariAmount = amount ?: MicroTari(BigInteger.valueOf(innerAmount.toLong())).takeIf { innerAmount != Double.MIN_VALUE }

        addFragment(AddAmountFragment.newInstance(contact, tariAmount))
    }

    private fun toChatDetail(walletAddress: TariWalletAddress, isNew: Boolean) {
        if (isNew) {
            onBackPressed()
        }

        addFragment(ChatDetailsFragment.newInstance(walletAddress))
    }

    private fun addFragment(fragment: CommonFragment<*, *>, bundle: Bundle? = null, isRoot: Boolean = false, withAnimation: Boolean = true) =
        activity.addFragment(fragment, bundle, isRoot, withAnimation)

    //popup fragment
    private fun popUpTo(tag: String) = activity.popUpTo(tag)

    companion object {
        const val PARAMETER_NOTE = "note"
        const val PARAMETER_AMOUNT = "amount"
        const val PARAMETER_TRANSACTION = "transaction_data"
        const val PARAMETER_CONTACT = "tari_contact_dto_args"

        const val INDEX_HOME = 0
        const val INDEX_CONTACT_BOOK = 1
        const val INDEX_CHAT = 2
        const val INDEX_SETTINGS = 3
        const val NO_SMOOTH_SCROLL = false
    }
}