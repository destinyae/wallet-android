package com.tari.android.wallet.ui.fragment.tx.history.contact

import androidx.lifecycle.SavedStateHandle
import com.tari.android.wallet.data.tx.TxRepository
import com.tari.android.wallet.extension.collectFlow
import com.tari.android.wallet.model.Tx
import com.tari.android.wallet.navigation.Navigation
import com.tari.android.wallet.navigation.TariNavigator
import com.tari.android.wallet.ui.common.CommonViewModel
import com.tari.android.wallet.ui.common.gyphy.presentation.GifViewModel
import com.tari.android.wallet.ui.common.gyphy.repository.GifRepository
import com.tari.android.wallet.ui.fragment.contactBook.data.ContactsRepository
import com.tari.android.wallet.ui.fragment.contactBook.data.contacts.ContactDto
import com.tari.android.wallet.ui.fragment.tx.adapter.TxViewHolderItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class ContactTxHistoryViewModel(savedState: SavedStateHandle) : CommonViewModel() {

    @Inject
    lateinit var txRepository: TxRepository

    @Inject
    lateinit var contactsRepository: ContactsRepository

    @Inject
    lateinit var gifRepository: GifRepository

    init {
        component.inject(this)
    }

    private val _uiState = MutableStateFlow(
        ContactTxHistoryModel.UiState(
            selectedContact = contactsRepository.getByUuid(savedState.get<ContactDto>(TariNavigator.PARAMETER_CONTACT)!!.uuid),
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        collectFlow(txRepository.allTxs) { txs ->
            _uiState.update { uiState ->
                uiState.copy(txList = txs.filter { it.tx.tariContact.walletAddress == uiState.selectedContact.walletAddress }
                    .map { it.copy(contact = uiState.selectedContact) }
                    .map { txDto ->
                        TxViewHolderItem(
                            txDto = txDto,
                            gifViewModel = GifViewModel(gifRepository)
                        )
                    }
                )
            }
        }
    }

    fun onSendTariClick() {
        tariNavigator.navigate(Navigation.TxListNavigation.ToSendTariToUser(uiState.value.selectedContact))
    }

    fun onTransactionClick(tx: Tx) {
        tariNavigator.navigate(Navigation.TxListNavigation.ToTxDetails(tx))
    }
}
