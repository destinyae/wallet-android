/**
 * Downloads JNI binaries as version specified in the BuildConfig file
 */
tasks.register("downloadLibwallet") {
    val rootDir = "${rootProject.projectDir}/libwallet/"
    val armDir = "${rootProject.projectDir}/libwallet/arm64-v8a/"
    val x64Dir = "${rootProject.projectDir}/libwallet/x86_64/"

    inputs.property("version", BuildConfig.LibWallet.libwalletVersion)
    outputs.dir(rootDir)

    doLast {
        logger.info("Downloading binaries with version ${BuildConfig.LibWallet.libwalletVersion}")
        println("Downloading binaries with version ${BuildConfig.LibWallet.libwalletVersion}")

        val files = listOf(
            BuildConfig.LibWallet.libwalletHeader,
            BuildConfig.LibWallet.libwalletArmA,
            BuildConfig.LibWallet.libwalletx64A,
        )

        download(BuildConfig.LibWallet.headerFileUrl, rootDir)
        copyFile(rootDir, rootDir, BuildConfig.LibWallet.libwalletHeader, "wallet.h")

        download(BuildConfig.LibWallet.x64AFileUrl, rootDir)
        copyFile(rootDir, x64Dir, BuildConfig.LibWallet.libwalletx64A, "libminotari_wallet_ffi.a")

        download(BuildConfig.LibWallet.armAFileUrl, rootDir)
        copyFile(rootDir, armDir, BuildConfig.LibWallet.libwalletArmA, "libminotari_wallet_ffi.a")

        delete {
            delete(fileTree(rootDir).apply {
                include(files)
            })
        }
    }
}

fun Project.download(fileUrl: String, destPath: String) {
    println("Downloading $fileUrl")
    val destFile = File(destPath)
    ant.invokeMethod("get", mapOf("src" to fileUrl, "dest" to destFile))
}

fun Project.copyFile(fromDir: String, toDir: String, oldFileName: String, newFileName: String): WorkResult {
    println("Copying $oldFileName to $toDir$newFileName")
    return copy {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from("$fromDir$oldFileName")
        rename { currentFileName -> if (currentFileName == oldFileName) newFileName else currentFileName }
        into(toDir)
    }
}
