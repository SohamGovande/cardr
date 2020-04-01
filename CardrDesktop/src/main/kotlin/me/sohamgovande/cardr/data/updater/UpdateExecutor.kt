package me.sohamgovande.cardr.data.updater

import javafx.application.Platform
import me.sohamgovande.cardr.util.*
import org.apache.logging.log4j.LogManager
import java.awt.Desktop
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

class UpdateExecutor(private val version: CardrVersion) {

    var messageHandler = { _: String -> Unit }
    var onClose = { Unit }

    fun update() {
        val installerPath = getInstallerFilePath()

        logger.info("Downloading new installer file...")
        messageHandler("Downloading the new cardr installer. This may take several minutes.")
        downloadFileFromURL(version.getURL(), installerPath.toFile(), logger)

        messageHandler("Finished installer download.")
        initInstallerFile(installerPath)
        if (getOSType() == OS.MAC) {
            Platform.runLater {
                onClose()
                showInfoDialogBlocking("Please read the instructions below.", "1. Once you click OK, a Finder window will appear containing the new cardr update. \n2. To install the update, RIGHT CLICK the file called '${version.getInstallerName()}' and select 'Open' in the action menu. \n3. Finally, click 'Open Anyway' in the dialog box that follows. \n\nClick OK to confirm you have read this message.")
                Desktop.getDesktop().browse(Paths.get(System.getProperty("cardr.data.dir", "Cardr Updates")).toUri())
                exitProcess(0)
            }
        } else {
            Desktop.getDesktop().open(installerPath.toFile())
            exitProcess(0)
        }
    }

    private fun getInstallerFilePath(): Path {
        val path = Paths.get(System.getProperty("cardr.data.dir"), "Cardr Updates", version.getInstallerName())
        path.parent.toFile().mkdir()
        return path
    }

    private fun initInstallerFile(installerPath: Path) {
        if (getOSType() == OS.MAC) {
            makeFileExecutableViaChmod(installerPath.toFile().absolutePath, logger)
        }
    }

    companion object {
        val logger = LogManager.getLogger(UpdateExecutor::class.java)
    }
}
