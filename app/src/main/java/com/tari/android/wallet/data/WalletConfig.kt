package com.tari.android.wallet.data

import android.content.Context
import com.tari.android.wallet.data.sharedPrefs.network.NetworkPrefRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletConfig @Inject constructor(val context: Context, val networkRepository: NetworkPrefRepository) {
    val walletDBName: String = "tari_wallet"
    val walletDBFullFileName: String = "$walletDBName.sqlite3"

    private val logFilePrefix = "tari_aurora"
    private val logFileExtension = "log"
    private val logFilesDirName = "tari_logs"

    /**
     * The directory in which the wallet files reside.
     */
    fun getWalletFilesDirPath(): String = context.filesDir.absolutePath + "/" + networkRepository.currentNetwork.network.uriComponent
    val walletDatabaseFilePath: String = File(getWalletFilesDirPath(), walletDBFullFileName).absolutePath

    /**
     * The directory in which the wallet log files reside.
     */
    fun getWalletLogFilesDirPath(): String {
        val logFilesDir = File(getWalletFilesDirPath(), logFilesDirName)
        if (!logFilesDir.exists()) {
            logFilesDir.mkdir()
        } else { // delete older log files
            val files = logFilesDir.listFiles()?.filter { !it.name.contains(logFilePrefix) }
            files?.forEach { it.delete() }
        }
        return logFilesDir.absolutePath
    }

    fun getWalletLogFilePath(): String = getOrCreateFilePath(getWalletLogFilesDirPath(), "$logFilePrefix.$logFileExtension")

    private fun getOrCreateFilePath(dirPath: String, fileName: String): String {
        val folder = File(dirPath)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val file = File(dirPath, fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file.absolutePath
    }

    fun getWalletTempDirPath(): String {
        val tempDir = File(getWalletFilesDirPath(), "temp")
        if (!tempDir.exists()) tempDir.mkdir()
        return tempDir.absolutePath
    }
}