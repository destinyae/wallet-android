package com.tari.android.wallet.ui.fragment.contact_book.details.adapter

import com.tari.android.wallet.ui.common.recyclerView.CommonAdapter
import com.tari.android.wallet.ui.common.recyclerView.CommonViewHolderItem
import com.tari.android.wallet.ui.common.recyclerView.ViewHolderBuilder
import com.tari.android.wallet.ui.common.recyclerView.viewHolders.SpaceVerticalViewHolder
import com.tari.android.wallet.ui.common.recyclerView.viewHolders.TitleViewHolder
import com.tari.android.wallet.ui.fragment.contact_book.details.adapter.contactType.ContactTypeViewHolder
import com.tari.android.wallet.ui.fragment.contact_book.details.adapter.profile.ContactProfileViewHolder
import com.tari.android.wallet.ui.fragment.settings.allSettings.row.SettingsRowViewHolder
import com.tari.android.wallet.ui.fragment.settings.allSettings.divider.SettingsDividerViewHolder
import com.tari.android.wallet.ui.fragment.settings.allSettings.title.SettingsTitleViewHolder

class ContactDetailsAdapter : CommonAdapter<CommonViewHolderItem>() {
    override var viewHolderBuilders: List<ViewHolderBuilder> = listOf(
        TitleViewHolder.getBuilder(),
        ContactProfileViewHolder.getBuilder(),
        SettingsDividerViewHolder.getBuilder(),
        SettingsRowViewHolder.getBuilder(),
        SettingsTitleViewHolder.getBuilder(),
        SpaceVerticalViewHolder.getBuilder(),
        ContactTypeViewHolder.getBuilder(),
    )
}