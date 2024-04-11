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
package com.tari.android.wallet.di

import com.tari.android.wallet.application.WalletManager
import com.tari.android.wallet.application.baseNodes.BaseNodesManager
import com.tari.android.wallet.data.WalletConfig
import com.tari.android.wallet.data.sharedPrefs.SharedPrefsRepository
import com.tari.android.wallet.data.sharedPrefs.baseNode.BaseNodeSharedRepository
import com.tari.android.wallet.data.sharedPrefs.network.NetworkRepository
import com.tari.android.wallet.data.sharedPrefs.security.SecurityPrefRepository
import com.tari.android.wallet.data.sharedPrefs.tariSettings.TariSettingsSharedRepository
import com.tari.android.wallet.service.seedPhrase.SeedPhraseRepository
import com.tari.android.wallet.tor.TorConfig
import com.tari.android.wallet.tor.TorProxyManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class WalletModule {

    @Provides
    @Singleton
    fun provideWalletManager(
        walletConfig: WalletConfig,
        torConfig: TorConfig,
        torProxyManager: TorProxyManager,
        sharedPrefsWrapper: SharedPrefsRepository,
        baseNodeSharedRepository: BaseNodeSharedRepository,
        seedPhraseRepository: SeedPhraseRepository,
        networkRepository: NetworkRepository,
        securityPrefRepository: SecurityPrefRepository,
        tariSettingsSharedRepository: TariSettingsSharedRepository,
        baseNodesManager: BaseNodesManager
    ): WalletManager = WalletManager(
        walletConfig,
        torProxyManager,
        sharedPrefsWrapper,
        baseNodeSharedRepository,
        seedPhraseRepository,
        networkRepository,
        tariSettingsSharedRepository,
        securityPrefRepository,
        baseNodesManager,
        torConfig
    )
}
