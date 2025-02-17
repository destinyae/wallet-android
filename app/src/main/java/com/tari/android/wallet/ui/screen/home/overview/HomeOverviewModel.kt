package com.tari.android.wallet.ui.screen.home.overview

import com.tari.android.wallet.model.BalanceInfo
import com.tari.android.wallet.model.EmojiId
import com.tari.android.wallet.ui.screen.tx.adapter.TxViewHolderItem
import com.tari.android.wallet.util.extension.toMicroTari

class HomeOverviewModel {
    data class UiState(
        val txList: List<TxViewHolderItem> = emptyList(),
        val balance: BalanceInfo = BalanceInfo(
            availableBalance = 0.toMicroTari(),
            pendingIncomingBalance = 0.toMicroTari(),
            pendingOutgoingBalance = 0.toMicroTari(),
            timeLockedBalance = 0.toMicroTari(),
        ),

        val avatarEmoji: EmojiId,
        val emojiMedium: EmojiId,
    )
}