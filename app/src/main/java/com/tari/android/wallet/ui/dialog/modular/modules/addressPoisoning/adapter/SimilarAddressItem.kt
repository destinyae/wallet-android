package com.tari.android.wallet.ui.dialog.modular.modules.addressPoisoning.adapter

import com.tari.android.wallet.ui.common.recyclerView.CommonViewHolderItem
import com.tari.android.wallet.ui.fragment.contact_book.address_poisoning.SimilarAddressDto
import com.tari.android.wallet.ui.fragment.contact_book.data.contacts.FFIContactDto
import java.util.Date

data class SimilarAddressItem(
    val dto: SimilarAddressDto,
    val lastItem: Boolean = false,
) : CommonViewHolderItem() {
    override val viewHolderUUID
        get() = dto.contactDto.uuid

    val ffiContact: FFIContactDto?
        get() = dto.contactDto.getFFIDto()
    val contactName: String
        get() = dto.contactDto.contact.getAlias()
    val numberOfTransaction: Int
        get() = dto.numberOfTransaction
    val lastTransactionDate: Date?
        get() = dto.lastTransactionTimestampMillis?.let { Date(it) }
    val trusted: Boolean
        get() = dto.trusted

    var selected: Boolean = false
}