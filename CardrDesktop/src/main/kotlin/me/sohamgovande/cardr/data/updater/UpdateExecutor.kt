package me.sohamgovande.cardr.data.updater

import javafx.application.Platform
import me.sohamgovande.cardr.util.*
import net.lingala.zip4j.ZipFile
import org.apache.logging.log4j.LogManager
import java.awt.Desktop
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

class UpdateExecutor(private val version: CardrVersion) {

    var messageHandler = { _: String -> Unit }
    var onClose = { Unit }

    @Throws(Exception::class)
    fun update() {
        val downloadPath = getInstallerFilePath()

        logger.info("Downloading new installer file...")
        messageHandler("Downloading the new cardr installer. This may take several minutes.")
        downloadFileFromURL(version.getURL(), downloadPath.toFile(), logger)

        initInstallerFile(downloadPath)
        messageHandler("Finished installer download.")

        if (getOSType() == OS.MAC) {
            Platform.runLater {
                onClose()
                showInfoDialogBlocking("Please read the instructions below.", "1. Once you click OK, a Finder window will appear containing the new cardr update. \n2. To install the update, double click the latest version PKG installer.\n\nClick OK to confirm you have read this message.")
                Desktop.getDesktop().browse(Paths.get(System.getProperty("cardr.data.dir", "Cardr Updates")).toUri())
                exitProcess(0)
            }
        } else {
            Desktop.getDesktop().open(Paths.get(System.getProperty("cardr.data.dir"), "Cardr Updates", version.getFinalFilename()).toFile())
            exitProcess(0)
        }
    }

    private fun getInstallerFilePath(): Path {
        val path = Paths.get(System.getProperty("cardr.data.dir"), "Cardr Updates", version.getDownloadFilename())
        path.parent.toFile().mkdir()
        return path
    }

    private fun initInstallerFile(download: Path) {
        if (version.shouldExtract()) {
            messageHandler("Extracting zipped file...")
            val downloadFile = download.toFile()
            val zipFile = ZipFile(downloadFile)
            zipFile.extractAll(Paths.get(System.getProperty("cardr.data.dir"), "Cardr Updates").toFile().canonicalPath)
            downloadFile.deleteOnExit()
        }
    }

    companion object {
        val logger = LogManager.getLogger(UpdateExecutor::class.java)
    }
}
