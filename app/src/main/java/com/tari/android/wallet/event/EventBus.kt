/**
 * Copyright 2020 The Tari Project
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of
 * its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tari.android.wallet.event

import com.tari.android.wallet.application.WalletState
import com.tari.android.wallet.infrastructure.backup.BackupsState
import com.tari.android.wallet.model.BalanceInfo
import com.tari.android.wallet.model.recovery.WalletRestorationResult
import com.tari.android.wallet.network.NetworkConnectionState
import com.tari.android.wallet.service.baseNode.BaseNodeState
import com.tari.android.wallet.service.baseNode.BaseNodeSyncState
import com.tari.android.wallet.tor.TorProxyState

/**
 * Event bus for the pub/sub model.
 *
 * Subscription example:
 *
 * EventBus.subscribe<EventClass>(this) {
 *      // op.s
 * }
 */
object EventBus : GeneralEventBus() {

    //todo looks like better to have it into appropriate classes than here
    val torProxyState = BehaviorEventBus<TorProxyState>()

    val balanceState = BehaviorEventBus<BalanceInfo>()

    val walletState = BehaviorEventBus<WalletState>()

    val networkConnectionState = BehaviorEventBus<NetworkConnectionState>()

    val backupState = BehaviorEventBus<BackupsState>()

    val baseNodeState = BehaviorEventBus<BaseNodeState>()

    val baseNodeSyncState = BehaviorEventBus<BaseNodeSyncState>()

    val walletRestorationState = BehaviorEventBus<WalletRestorationResult>()

    init {
        baseNodeSyncState.post(BaseNodeSyncState.Syncing)
    }

    fun unsubscribeAll(subscriber: Any) {
        EventBus.unsubscribe(subscriber)
        torProxyState.unsubscribe(subscriber)
        walletState.unsubscribe(subscriber)
        networkConnectionState.unsubscribe(subscriber)
        backupState.unsubscribe(subscriber)
        baseNodeState.unsubscribe(subscriber)
        baseNodeSyncState.unsubscribe(subscriber)
        walletRestorationState.unsubscribe(subscriber)
    }

    override fun clear() {
        super.clear()
        torProxyState.clear()
        walletState.clear()
        networkConnectionState.clear()
        backupState.clear()
        baseNodeState.clear()
        baseNodeSyncState.clear()
        walletRestorationState.clear()
    }
}

