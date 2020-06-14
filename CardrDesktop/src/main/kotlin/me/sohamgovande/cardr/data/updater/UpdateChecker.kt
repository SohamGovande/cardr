package me.sohamgovande.cardr.data.updater

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.layout.Region
import me.sohamgovande.cardr.CHROME_OCR_MODE
import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.core.ui.windows.UpdateWindow
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.data.urls.UrlHelper
import me.sohamgovande.cardr.util.*
import org.apache.logging.log4j.LogManager
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

class UpdateChecker(private val ui: CardrUI) {
    private fun showUpdateDialog(version: CardrVersion) {
        if (Prefs.get().hideUpdateDialog)
            return

        val neverShowBT = ButtonType("Hide notifications", ButtonBar.ButtonData.CANCEL_CLOSE)
        val updateBT = ButtonType("Update now", ButtonBar.ButtonData.OK_DONE)
        val remindBT = ButtonType("Update later", ButtonBar.ButtonData.CANCEL_CLOSE)
        val seeWhatsNewBT = ButtonType("See what's new", ButtonBar.ButtonData.CANCEL_CLOSE)

        val alert = Alert(Alert.AlertType.CONFIRMATION, "", updateBT, remindBT, seeWhatsNewBT, neverShowBT)
        alert.title = "Update cardr"
        alert.headerText = "A new version of cardr is available!"
        alert.contentText = "Version ${version.name} has new features, bug fixes, and security updates. Would you like to download it?"
        alert.dialogPane.minHeight = Region.USE_PREF_SIZE

        val result = alert.showAndWait()
        var noUpdate = false
        if (result.isPresent && result.get() == updateBT) {
            UpdateWindow(version).show()
        } else if (result.isPresent && result.get() == seeWhatsNewBT) {
            UrlHelper.browse("changelog")
            showUpdateDialog(version)
            noUpdate = true
        } else if (result.isPresent && result.get() == neverShowBT) {
            Prefs.get().hideUpdateDialog = true
            ui.menubarHelper.hideUpdateWarningMI.isSelected = true
            Prefs.save()
            showInfoDialogBlocking("Message will no longer be displayed.", "You can revert this setting under 'Settings > Messages > Hide update dialog'.")
            noUpdate = true
        }
        if (noUpdate)
            noUpdate()
    }

    fun checkForMessages(): Boolean {
        // Scan server for messages
        try {
            val msgsRaw = makeRequest(UrlHelper.get("messages") + "?version=${CardrDesktop.CURRENT_VERSION}&build=${CardrDesktop.CURRENT_VERSION_INT}&email=${Prefs.get().emailAddress}&token=${Prefs.get().accessToken}")
            val msgsJson = JsonParser().parse(msgsRaw) as JsonArray
            val queue = arrayListOf<() -> Unit>()
            var size = 0
            logger.info("Received messages data $msgsJson")
            for (msgJson in msgsJson) {
                val msgJsonObj = msgJson.asJsonObject
                val type = msgJsonObj["type"].asString
                val header = msgJsonObj["header"]?.asString ?: ""
                val body = msgJsonObj["body"]?.asString ?: ""
                val chance = msgJsonObj["chance"]?.asDouble ?: 1.0
                if (chance != 1.0 && Math.random() >= chance)
                    continue

                when (type) {
                    "error" -> {
                        queue.add { showErrorDialogBlocking(header, body) }
                        size++
                    }
                    "info" -> {
                        queue.add { showInfoDialogBlocking(header, body) }
                        size++
                    }
                    "fatal_error" -> {
                        queue.add { showErrorDialogBlocking(header, body); exitProcess(0) }
                        size++
                    }
                    "hidden" -> {
                        queue.add { logger.info("Received hidden message: $msgJsonObj") }
                    }
                }
            }
            Platform.runLater {
                for (func in queue)
                    func()
            }
            if (size > 0)
                return true
        } catch (e: Exception) {
            logger.error("Unable to check server for messages", e)
        }
        return false
    }

    fun showMOTD(): Boolean {
        // Message of the day
        if (Prefs.get().showTips && !CHROME_OCR_MODE) {
            val today = currentDate().format(DateTimeFormatter.ISO_DATE)
            if (Prefs.get().lastMOTD != today) {
                Platform.runLater { me.sohamgovande.cardr.core.ui.motd.showMOTD() }
                Prefs.get().lastMOTD = today
                Prefs.save()
                return true
            }
        }
        return false
    }

    fun noUpdate() {
        if (!checkForMessages())
            showMOTD()
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

            if (latestVersion.build > CardrDesktop.CURRENT_VERSION_INT || CardrDesktop.FORCE_AUTO_UPDATE) {
                logger.info("Latest version is $latestVersion - needs to update!")

                Platform.runLater { showUpdateDialog(latestVersion) }
            } else {
                logger.info("Latest version is $latestVersion - no need to update.")
                // Already updated
            }
        } catch (e: Exception) {
            logger.error("Error checking for updates", e)
        }
        noUpdate()
    }

    companion object {
        val logger = LogManager.getLogger(UpdateChecker::class.java)
    }
}
