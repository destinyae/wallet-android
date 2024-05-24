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
package com.tari.android.wallet.data.sharedPrefs

import android.content.SharedPreferences
import com.tari.android.wallet.data.repository.CommonRepository
import com.tari.android.wallet.data.sharedPrefs.addressPoisoning.AddressPoisoningPrefRepository
import com.tari.android.wallet.data.sharedPrefs.baseNode.BaseNodePrefRepository
import com.tari.android.wallet.data.sharedPrefs.delegates.SharedPrefBooleanDelegate
import com.tari.android.wallet.data.sharedPrefs.delegates.SharedPrefStringDelegate
import com.tari.android.wallet.data.sharedPrefs.network.NetworkPrefRepository
import com.tari.android.wallet.data.sharedPrefs.network.formatKey
import com.tari.android.wallet.data.sharedPrefs.security.SecurityPrefRepository
import com.tari.android.wallet.data.sharedPrefs.securityStages.SecurityStagesPrefRepository
import com.tari.android.wallet.data.sharedPrefs.sentry.SentryPrefRepository
import com.tari.android.wallet.data.sharedPrefs.tariSettings.TariSettingsPrefRepository
import com.tari.android.wallet.data.sharedPrefs.tor.TorPrefRepository
import com.tari.android.wallet.data.sharedPrefs.contacts.ContactPrefRepository
import com.tari.android.wallet.data.sharedPrefs.backup.BackupPrefRepository
import com.tari.android.wallet.data.sharedPrefs.chat.ChatsPrefRepository
import com.tari.android.wallet.data.sharedPrefs.yat.YatPrefRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Provides easy access to the shared preferences.
 *
 * @author The Tari Development Team
 */
//todo Need to thing about reactive realization

@Singleton
class CorePrefRepository @Inject constructor(
    sharedPrefs: SharedPreferences,
    networkRepository: NetworkPrefRepository,
    private val backupSettingsRepository: BackupPrefRepository,
    private val baseNodeSharedRepository: BaseNodePrefRepository,
    private val yatSharedRepository: YatPrefRepository,
    private val torSharedRepository: TorPrefRepository,
    private val tariSettingsSharedRepository: TariSettingsPrefRepository,
    private val securityStagesRepository: SecurityStagesPrefRepository,
    private val contactSharedPrefRepository: ContactPrefRepository,
    private val sentryPrefRepository: SentryPrefRepository,
    private val securityPrefRepository: SecurityPrefRepository,
    private val addressPoisoningSharedRepository: AddressPoisoningPrefRepository,
    private val chatPrefRepository: ChatsPrefRepository,
) : CommonRepository(networkRepository) {

    private object Key {
        const val PUBLIC_KEY_HEX_STRING = "tari_wallet_public_key_hex_string"
        const val EMOJI_ID = "tari_wallet_emoji_id_"
        const val NAME = "tari_wallet_name_"
        const val SURNAME = "tari_wallet_surname_"
        const val ONBOARDING_STARTED = "tari_wallet_onboarding_started"
        const val ONBOARDING_AUTH_SETUP_COMPLETED = "tari_wallet_onboarding_auth_setup_completed"
        const val ACTION_MENU_SIDE = "tari_wallet_action_menu_side"
        const val ONBOARDING_AUTH_SETUP_STARTED = "tari_wallet_onboarding_auth_setup_started"
        const val ONBOARDING_COMPLETED = "tari_wallet_onboarding_completed"
        const val ONBOARDING_DISPLAYED_AT_HOME = "tari_wallet_onboarding_displayed_at_home"
        const val IS_DATA_CLEARED = "tari_is_data_cleared"
    }

    var publicKeyHexString: String? by SharedPrefStringDelegate(sharedPrefs, this, formatKey(Key.PUBLIC_KEY_HEX_STRING))

    var emojiId: String? by SharedPrefStringDelegate(sharedPrefs, this, formatKey(Key.EMOJI_ID))

    var name: String? by SharedPrefStringDelegate(sharedPrefs, this, formatKey(Key.NAME))

    var surname: String? by SharedPrefStringDelegate(sharedPrefs, this, formatKey(Key.SURNAME))

    var onboardingStarted: Boolean by SharedPrefBooleanDelegate(sharedPrefs, this, formatKey(Key.ONBOARDING_STARTED))

    var onboardingCompleted: Boolean by SharedPrefBooleanDelegate(sharedPrefs, this, formatKey(Key.ONBOARDING_COMPLETED))

    var onboardingAuthSetupStarted: Boolean by SharedPrefBooleanDelegate(sharedPrefs, this, formatKey(Key.ONBOARDING_AUTH_SETUP_STARTED))

    var onboardingAuthSetupCompleted: Boolean by SharedPrefBooleanDelegate(sharedPrefs, this, formatKey(Key.ONBOARDING_AUTH_SETUP_COMPLETED))

    var actionMenuSide: Boolean by SharedPrefBooleanDelegate(sharedPrefs, this, formatKey(Key.ACTION_MENU_SIDE))

    val onboardingAuthWasInterrupted: Boolean
        get() = onboardingAuthSetupStarted && (!onboardingAuthSetupCompleted || securityPrefRepository.pinCode == null)

    val onboardingWasInterrupted: Boolean
        get() = onboardingStarted && !onboardingCompleted

    var onboardingDisplayedAtHome: Boolean by SharedPrefBooleanDelegate(sharedPrefs, this, formatKey(Key.ONBOARDING_DISPLAYED_AT_HOME))

    var isDataCleared: Boolean by SharedPrefBooleanDelegate(sharedPrefs, this, formatKey(Key.IS_DATA_CLEARED), true)

    fun clear() {
        baseNodeSharedRepository.clear()
        backupSettingsRepository.clear()
        yatSharedRepository.clear()
        torSharedRepository.clear()
        tariSettingsSharedRepository.clear()
        securityStagesRepository.clear()
        contactSharedPrefRepository.clear()
        sentryPrefRepository.clear()
        securityPrefRepository.clear()
        addressPoisoningSharedRepository.clear()
        chatPrefRepository.clear()
        publicKeyHexString = null
        emojiId = null
        onboardingStarted = false
        onboardingCompleted = false
        onboardingAuthSetupStarted = false
        onboardingAuthSetupCompleted = false
        onboardingDisplayedAtHome = false

    }

    fun generateDatabasePassphrase(): String {
        val generatedString = java.lang.StringBuilder()

        while (generatedString.length < 32) {
            val nextChar = Char(Random.nextInt(Char.MIN_VALUE.code, Char.MAX_VALUE.code))
            if (isBrokenCharForPassphrase(nextChar)) continue
            generatedString.append(nextChar)
        }
        return generatedString.toString()
    }

    // Runs when user manually clear the application data
    fun checkIfIsDataCleared(): Boolean {
        val isCleared = isDataCleared
        if (isCleared) {
            clear()
            isDataCleared = false
        }
        return isCleared
    }

    companion object {
        fun isBrokenCharForPassphrase(char: Char): Boolean = char.code == 0 || char.code in (55296..57343)
    }
}