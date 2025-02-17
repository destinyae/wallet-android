package com.tari.android.wallet.ui.screen.home

import android.net.Uri
import com.tari.android.wallet.navigation.Navigation
import com.tari.android.wallet.ui.common.CommonViewModel
import com.tari.android.wallet.data.contacts.ContactsRepository
import com.tari.android.wallet.infrastructure.ShareManager
import javax.inject.Inject

class HomeViewModel : CommonViewModel() {

    @Inject
    lateinit var contactsRepository: ContactsRepository

    val shareViewModel = ShareManager()

    init {
        component.inject(this)

        shareViewModel.tariBluetoothServer.doOnRequiredPermissions = { permissions, action ->
            permissionManager.runWithPermission(permissions, false, action)
        }

        shareViewModel.tariBluetoothClient.doOnRequiredPermissions = { permissions, action ->
            permissionManager.runWithPermission(permissions, false, action)
        }
    }

    fun navigateToAuth(uri: Uri?) {
        tariNavigator.navigate(Navigation.Auth.AuthScreen(uri))
    }
}