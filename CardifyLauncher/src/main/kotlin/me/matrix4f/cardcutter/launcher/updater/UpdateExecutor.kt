package me.matrix4f.cardcutter.launcher.updater

import me.matrix4f.cardcutter.launcher.CardifyLauncher
import me.matrix4f.cardcutter.launcher.util.downloadFileFromURL
import org.apache.logging.log4j.LogManager
import java.nio.file.Paths


class UpdateExecutor(private val version: CardifyVersionData, private val launcher: CardifyLauncher) {

    var messageHandler = { _: String -> Unit }
    var onClose = { Unit }

    fun update() {
        messageHandler("Creating file handles...")
        val normalFolderPath = Paths.get(System.getProperty("user.home"), "AppData", "Local", "CardifyDebate").toAbsolutePath()
        if (!normalFolderPath.toFile().exists()) {
            logger.error("Not running in correct context - unable to locate app directory - currently set to ${normalFolderPath.toFile().canonicalPath}")
        }

        val zipPath = Paths.get(System.getProperty("cardifydebate.data.dir"), "CardifyDebate.jar")
        val zipPathFile = zipPath.toFile()

        messageHandler("Downloading Cardify update...")
        downloadFileFromURL(version.cardifyVersion.jarUrl, zipPathFile, logger)

        launcher.launchCardify(true)
    }

    companion object {
        val logger = LogManager.getLogger(UpdateExecutor::class.java)
    }
}