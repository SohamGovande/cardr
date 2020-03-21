package me.matrix4f.cardcutter.data.updater

import javafx.application.Platform
import me.matrix4f.cardcutter.util.*
import net.lingala.zip4j.ZipFile
import org.apache.logging.log4j.LogManager
import java.awt.Desktop
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

class UpdateExecutor(private val version: CardifyVersion) {

    var messageHandler = { _: String -> Unit }
    var onClose = { Unit }

    fun update() {
        val zipPath = getInstallerFilePath()
        val zipPathFile = zipPath.toFile()

        logger.info("Downloading new installer file...")
        messageHandler("Downloading new Cardify installer. This may take several minutes.")
        downloadFileFromURL(version.getURL(), zipPathFile, logger)

        logger.info("Extracting zipped executable...")
        messageHandler("Unzipping executable.")
        val zipFile = ZipFile(zipPathFile)
        zipFile.extractAll(zipPath.parent.toFile().absolutePath)
        zipPathFile.deleteOnExit()

        messageHandler("Finished installer download.")
        val installerPath = initInstallerFile(zipPath)
        if (getOSType() == OS.MAC) {
            Platform.runLater {
                onClose()
                showInfoDialogBlocking("Please read the instructions below.", "1. Once you click OK, a Finder window will appear containing the new Cardify update. \n2. To install the update, RIGHT CLICK the file called '${version.getInstallerName()}' and select Open in the action menu. \n3. Finally, click 'Open' in the dialog box that follows. \n\nClick OK to confirm you have read this message.")
                Desktop.getDesktop().browse(installerPath.parent.toFile().toURI())
                exitProcess(0)
            }
        } else {
            val installerFile = installerPath.toFile()
            installerFile.setExecutable(true)
            executeCommandUnblocking("\"${installerFile.canonicalPath}\"", logger)
            Thread.sleep(500)
            exitProcess(0)
        }
    }

    private fun getInstallerFilePath(): Path {
        val path = Paths.get(System.getProperty("cardifydebate.data.dir"), "Cardify Updates", "CardifyNewVersion.zip")
        path.parent.toFile().mkdir()
        return path
    }

    private fun initInstallerFile(zipPath: Path): Path {
        val installerPath = Paths.get(zipPath.parent.toFile().absolutePath, version.getInstallerName())
        if (getOSType() == OS.MAC) {
            makeFileExecutableViaChmod(installerPath.toFile().absolutePath, logger)
        }
        return installerPath
    }

    companion object {
        val logger = LogManager.getLogger(UpdateExecutor::class.java)
    }
}