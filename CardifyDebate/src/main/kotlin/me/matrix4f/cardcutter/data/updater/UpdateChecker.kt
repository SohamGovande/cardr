package me.matrix4f.cardcutter.data.updater

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import me.matrix4f.cardcutter.CardifyDebate
import me.matrix4f.cardcutter.core.ui.windows.UpdateWindow
import me.matrix4f.cardcutter.util.CardifyException
import me.matrix4f.cardcutter.util.makeRequest
import org.apache.logging.log4j.LogManager
import java.awt.Desktop
import java.net.URL

class UpdateChecker {
    private fun showUpdateDialog(version: CardifyVersion) {
        val updateBT = ButtonType("Update Now", ButtonBar.ButtonData.OK_DONE)
        val remindBT = ButtonType("Remind Me Later", ButtonBar.ButtonData.CANCEL_CLOSE)
        val seeWhatsNewBT = ButtonType("See what's new", ButtonBar.ButtonData.CANCEL_CLOSE)

        val alert = Alert(Alert.AlertType.CONFIRMATION, "", updateBT, remindBT, seeWhatsNewBT)
        alert.title = "Update Cardify"
        alert.headerText = "A new version of Cardify is available!"
        alert.contentText = "Version ${version.name} is available for you to download. Would you like to download the update?"

        val result = alert.showAndWait()
        if (result.isPresent && result.get() == updateBT) {
            UpdateWindow(version).show()
//            Desktop.getDesktop().browse(URL("http://cardifydebate.x10.bz/download.html").toURI())
//            exitProcess(0)
        } else if (result.isPresent && result.get() == seeWhatsNewBT) {
            Desktop.getDesktop().browse(URL("http://cardifydebate.x10.bz/changelog.html").toURI())
        }
    }

    fun checkForUpdates() {
        try {
            val data = makeRequest("http://cardifydebate.x10.bz/data/VersionInfo.json")
            val gson = GsonBuilder().setPrettyPrinting().setLenient().create()
            val jsonParent = JsonParser().parse(data) as JsonObject
            val latestVersion: CardifyVersion
            try {
                latestVersion = gson.fromJson(jsonParent["latestVersion"], CardifyVersion::class.java)
            } catch (e: Exception) {
                throw CardifyException("Unable to parse version info JSON")
            }

            if (latestVersion.build > CardifyDebate.CURRENT_VERSION_INT) {
                logger.info("Latest version is ${latestVersion} - needs to update!")

                Platform.runLater { showUpdateDialog(latestVersion) }
            } else {
                logger.info("Latest version is ${latestVersion} - no need to update.")
                // Already updated
            }
        } catch (e: Exception) {
            logger.error("Error checking for updates", e)
        }
    }

    companion object {
        val logger = LogManager.getLogger(UpdateChecker::class.java)
    }
}