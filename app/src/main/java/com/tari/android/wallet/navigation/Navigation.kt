package com.tari.android.wallet.navigation

import android.net.Uri
import com.tari.android.wallet.application.YatAdapter.ConnectedWallet
import com.tari.android.wallet.model.MicroTari
import com.tari.android.wallet.model.TariWalletAddress
import com.tari.android.wallet.model.tx.Tx
import com.tari.android.wallet.model.TxId
import com.tari.android.wallet.data.contacts.model.ContactDto
import com.tari.android.wallet.ui.screen.pinCode.PinCodeScreenBehavior
import com.tari.android.wallet.ui.screen.send.common.TransactionData

sealed class Navigation {

    data class EnterPinCode(val behavior: PinCodeScreenBehavior, val stashedPin: String? = null) : Navigation()
    data object ChangeBiometrics : Navigation()
    data class SplashScreen(val seedWords: List<String>? = null, val clearTop: Boolean = true, val uri: Uri? = null) : Navigation()
    data class Home(val uri: Uri? = null) : Navigation()
    data object BackToHome : Navigation()

    sealed class Auth : Navigation() {
        data class AuthScreen(val uri: Uri? = null) : Navigation()
        data object FeatureAuth : Navigation()
        data object BackAfterAuth : Navigation()
    }

    sealed class CustomBridge : Navigation() {
        data object UploadQrCode : CustomBridge()
    }

    sealed class BackupSettings : Navigation() {
        data object ToLearnMore : BackupSettings()
        data object ToWalletBackupWithRecoveryPhrase : BackupSettings()
        data object ToChangePassword : BackupSettings()
        data object ToConfirmPassword : BackupSettings()
    }

    sealed class VerifySeedPhrase : Navigation() {
        data class ToSeedPhraseVerification(val seedWords: List<String>) : VerifySeedPhrase()
    }

    sealed class TorBridge : Navigation() {
        data object ToCustomBridges : TorBridge()
    }

    sealed class TxList : Navigation() {
        data class ToTxDetails(val tx: Tx? = null, val txId: TxId? = null) : TxList()
        data object ToChat : TxList()
        data object ToAllSettings : TxList()
        data object ToUtxos : TxList()
        data class ToSendTariToUser(val contact: ContactDto, val amount: MicroTari? = null, val note: String = "") : TxList()
        data object HomeTransactionHistory : TxList()
        data object ToTransfer : TxList()
    }

    sealed class Chat : Navigation() {
        data object ToAddChat : Chat()
        data class ToChat(val walletAddress: TariWalletAddress, val isNew: Boolean) : Chat()
    }

    sealed class AddAmount : Navigation() {
        data class ContinueToAddNote(val transactionData: TransactionData) : AddAmount()
        data class ContinueToFinalizing(val transactionData: TransactionData) : AddAmount()
    }

    sealed class AllSettings : Navigation() {
        data object ToMyProfile : AllSettings()
        data object ToBugReporting : AllSettings()
        data object ToDataCollection : AllSettings()
        data object ToAbout : AllSettings()
        data object BackToBackupSettings : AllSettings()
        data class ToBackupSettings(val withAnimation: Boolean) : AllSettings()
        data object ToDeleteWallet : AllSettings()
        data object ToBackgroundService : AllSettings()
        data object ToScreenRecording : AllSettings()
        data object ToBluetoothSettings : AllSettings()
        data object ToThemeSelection : AllSettings()
        data object ToTorBridges : AllSettings()
        data object ToNetworkSelection : AllSettings()
        data object ToBaseNodeSelection : AllSettings()
        data object ToRequestTari : AllSettings()
    }

    sealed class InputSeedWords : Navigation() {
        data object ToRestoreFromSeeds : InputSeedWords()
        data object ToBaseNodeSelection : InputSeedWords()
    }

    sealed class ChooseRestoreOption : Navigation() {
        data object ToEnterRestorePassword : ChooseRestoreOption()
        data object ToRestoreWithRecoveryPhrase : ChooseRestoreOption()
    }

    sealed class ContactBook : Navigation() {
        data class ToContactDetails(val contact: ContactDto) : ContactBook()
        data object ToAddContact : ContactBook()
        data object ToAddPhoneContact : ContactBook()
        data class ToSendTari(val contact: ContactDto) : ContactBook()
        data object ToSelectTariUser : ContactBook()
        data class ToRequestTari(val contact: ContactDto) : ContactBook()
        data class ToExternalWallet(val connectedWallet: ConnectedWallet) : ContactBook()
        data class ToLinkContact(val contact: ContactDto) : ContactBook()
        data class ToContactTransactionHistory(val contact: ContactDto) : ContactBook()
        data object BackToContactBook : ContactBook()
    }
}
