package me.sohamgovande.cardr.data.updater

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.core.ui.windows.UpdateWindow
import me.sohamgovande.cardr.data.urls.UrlHelper
import me.sohamgovande.cardr.util.makeRequest
import org.apache.logging.log4j.LogManager

class UpdateChecker {
    private fun showUpdateDialog(version: CardrVersion) {
        val updateBT = ButtonType("Update Now", ButtonBar.ButtonData.OK_DONE)
        val remindBT = ButtonType("Remind Me Later", ButtonBar.ButtonData.CANCEL_CLOSE)
        val seeWhatsNewBT = ButtonType("See what's new", ButtonBar.ButtonData.CANCEL_CLOSE)

        val alert = Alert(Alert.AlertType.CONFIRMATION, "", updateBT, remindBT, seeWhatsNewBT)
        alert.title = "Update cardr"
        alert.headerText = "A new version of cardr is available!"
        alert.contentText = "Version ${version.name} has new features, bug fixes, and security updates. Would you like to download it?"

        val result = alert.showAndWait()
        if (result.isPresent && result.get() == updateBT) {
            UpdateWindow(version).show()
        } else if (result.isPresent && result.get() == seeWhatsNewBT) {
            UrlHelper.browse("changelog")
            showUpdateDialog(version)
        }
    }

    fun checkForUpdates() {
        try {
            val data = makeRequest(UrlHelper.get("versionInfo"))
            val gson = GsonBuilder().setPrettyPrinting().setLenient().create()
            val jsonParent = JsonParser().parse(data) as JsonObject
            val latestVersion: CardrVersion
            val jsonSuffix = if (CardrDesktop.RELEASE_MODE) "" else "Dev"
            logger.info("Read json data $jsonParent")
            try {
                latestVersion = gson.fromJson(jsonParent["latestVersion$jsonSuffix"], CardrVersion::class.java)
            } catch (e: Exception) {
                logger.error("Unable to parse version info JSON", e)
                return
            }

            if (latestVersion.build > CardrDesktop.CURRENT_VERSION_INT || CardrDesktop.FORCE_AUTOUPDATE) {
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
