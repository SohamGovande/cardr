package me.sohamgovande.cardr.launcher.updater

import me.sohamgovande.cardr.launcher.CardrLauncher
import me.sohamgovande.cardr.launcher.util.downloadFileFromURL
import org.apache.logging.log4j.LogManager
import java.nio.file.Paths


class UpdateExecutor(private val version: CardrVersionData, private val launcher: CardrLauncher) {

    var messageHandler = { _: String -> Unit }
    var onClose = { Unit }

    fun update() {
        val zipPath = Paths.get(System.getProperty("cardr.data.dir"), "Cardr.jar")
        val zipPathFile = zipPath.toFile()

        messageHandler("This may take 1-2 minutes.")
        downloadFileFromURL(version.cardrVersion.jarUrl, zipPathFile, logger)

        launcher.launchCardr(true)
    }

    companion object {
        val logger = LogManager.getLogger(UpdateExecutor::class.java)
    }
}