package com.tari.android.wallet.application.securityStage

import com.tari.android.wallet.application.securityStage.StagedWalletSecurityManager.StagedSecurityEffect.NoStagedSecurityPopUp
import com.tari.android.wallet.application.securityStage.StagedWalletSecurityManager.StagedSecurityEffect.ShowStagedSecurityPopUp
import com.tari.android.wallet.data.sharedPrefs.securityStages.DisabledTimestampsDto
import com.tari.android.wallet.data.sharedPrefs.securityStages.SecurityStagesRepository
import com.tari.android.wallet.data.sharedPrefs.securityStages.WalletSecurityStage
import com.tari.android.wallet.data.sharedPrefs.tariSettings.TariSettingsSharedRepository
import com.tari.android.wallet.extension.isAfterNow
import com.tari.android.wallet.model.BalanceInfo
import com.tari.android.wallet.model.MicroTari
import com.tari.android.wallet.ui.fragment.settings.backup.data.BackupSettingsRepository
import java.math.BigDecimal
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

val MINIMUM_STAGE_ONE_BALANCE = MicroTari((BigDecimal.valueOf(10_000) * MicroTari.precisionValue).toBigInteger())
val STAGE_TWO_THRESHOLD_BALANCE = MicroTari((BigDecimal.valueOf(100_000) * MicroTari.precisionValue).toBigInteger())
val SAFE_HOT_WALLET_BALANCE = MicroTari((BigDecimal.valueOf(500_000_000) * MicroTari.precisionValue).toBigInteger())
val MAX_HOT_WALLET_BALANCE = MicroTari((BigDecimal.valueOf(1_000_000_000) * MicroTari.precisionValue).toBigInteger())

@Singleton
class StagedWalletSecurityManager @Inject constructor(
    private val securityStagesRepository: SecurityStagesRepository,
    private val backupPrefsRepository: BackupSettingsRepository,
    private val tariSettingsSharedRepository: TariSettingsSharedRepository,
) {
    private val hasVerifiedSeedPhrase
        get() = tariSettingsSharedRepository.hasVerifiedSeedWords

    private val isBackupOn
        get() = backupPrefsRepository.getOptionList.any { it.isEnable }

    private val isBackupPasswordSet
        get() = !backupPrefsRepository.backupPassword.isNullOrEmpty()

    private val disabledTimestampSinceNow: Calendar
        get() = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, 7) }

    private var disabledTimestamps: MutableMap<WalletSecurityStage, Calendar>
        get() = securityStagesRepository.disabledTimestamps?.timestamps ?: DisabledTimestampsDto(mutableMapOf()).timestamps
        set(value) {
            securityStagesRepository.disabledTimestamps = DisabledTimestampsDto(value)
        }

    /**
     * Check the current security stage based on the balance and the user's security settings.
     */
    fun handleBalanceChange(balance: BalanceInfo): StagedSecurityEffect {
        val securityStage = checkSecurityStage(balance) ?: return NoStagedSecurityPopUp
        //todo Stage 3 is currently disabled
        if (securityStage == WalletSecurityStage.Stage3) return NoStagedSecurityPopUp
        if (isActionDisabled(securityStage)) return NoStagedSecurityPopUp

        updateTimestamp(securityStage)

        return ShowStagedSecurityPopUp(securityStage)
    }

    private fun updateTimestamp(securityStage: WalletSecurityStage) {
        val newTimestamp = disabledTimestampSinceNow
        disabledTimestamps = disabledTimestamps.also { it[securityStage] = newTimestamp }
    }

    /**
     * Returns null if no security stage is required.
     */
    private fun checkSecurityStage(balanceInfo: BalanceInfo): WalletSecurityStage? {
        val balance = balanceInfo.availableBalance

        return when {
            balance >= MINIMUM_STAGE_ONE_BALANCE && !hasVerifiedSeedPhrase -> WalletSecurityStage.Stage1A
            balance >= MINIMUM_STAGE_ONE_BALANCE && !isBackupOn -> WalletSecurityStage.Stage1B
            balance >= STAGE_TWO_THRESHOLD_BALANCE && !isBackupPasswordSet -> WalletSecurityStage.Stage2
            balance >= SAFE_HOT_WALLET_BALANCE -> WalletSecurityStage.Stage3
            else -> null
        }
    }

    private fun isActionDisabled(securityStage: WalletSecurityStage): Boolean {
        val timestamp = disabledTimestamps[securityStage] ?: return false
        if (timestamp.isAfterNow()) {
            return true
        }

        disabledTimestamps = disabledTimestamps.also { it.remove(securityStage) }
        return false
    }

    sealed class StagedSecurityEffect {
        data class ShowStagedSecurityPopUp(val stage: WalletSecurityStage) : StagedSecurityEffect()
        object NoStagedSecurityPopUp : StagedSecurityEffect()
    }
}