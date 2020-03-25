package me.matrix4f.cardcutter.updater

import me.matrix4f.cardcutter.util.downloadFileFromURL
import me.matrix4f.cardcutter.util.executeCommandUnblocking
import net.lingala.zip4j.ZipFile
import org.apache.logging.log4j.LogManager
import java.nio.file.Paths


class UpdateExecutor(private val version: CardifyVersionData) {

    var messageHandler = { _: String -> Unit }
    var onClose = { Unit }

    fun update() {
        messageHandler("Creating file handles...")
        val normalFolderPath = Paths.get(System.getProperty("user.home"), "AppData", "Local", "CardifyDebate").toAbsolutePath()
        if (!normalFolderPath.toFile().exists()) {
            logger.error("Not running in correct context - unable to locate app directory - currently set to ${normalFolderPath.toFile().canonicalPath}")
        }

        val zipPath = Paths.get(System.getProperty("cardifydebate.data.dir"), "TempPatches-${version.cardifyVersion.name}.zip")
        zipPath.parent.toFile().mkdir()
        val zipPathFile = zipPath.toFile()

        messageHandler("Downloading patch file...")
        downloadFileFromURL(version.cardifyVersion.patchZip, zipPathFile, logger)

        messageHandler("Extracting patch file...")
        val zipFile = ZipFile(zipPathFile)
        zipFile.extractAll(normalFolderPath.toFile().canonicalPath)
        zipPathFile.deleteOnExit()

        messageHandler("Done.")
        val restartCommand = version.updaterVersion.restartCmdWin.replace("%USER_HOME%", System.getProperty("user.home"))
        executeCommandUnblocking(restartCommand, logger)
    }

    companion object {
        val logger = LogManager.getLogger(UpdateExecutor::class.java)
    }
}