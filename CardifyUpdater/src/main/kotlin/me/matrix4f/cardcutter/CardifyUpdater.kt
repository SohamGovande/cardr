package me.matrix4f.cardcutter

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import javafx.application.Application
import javafx.stage.Stage
import me.matrix4f.cardcutter.ui.CardifyUpdaterUI
import me.matrix4f.cardcutter.updater.CardifyUpdaterVersion
import me.matrix4f.cardcutter.updater.CardifyVersion
import me.matrix4f.cardcutter.updater.CardifyVersionData
import me.matrix4f.cardcutter.util.CardifyException
import me.matrix4f.cardcutter.util.makeRequest
import me.matrix4f.cardcutter.util.showErrorDialog
import org.apache.logging.log4j.LogManager

class CardifyUpdater : Application() {

    private fun getLatestVersion(): CardifyVersionData {
        val logger = LogManager.getLogger(CardifyUpdater::class.java)
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
            val latestUpdaterVersion: CardifyUpdaterVersion
            try {
                latestUpdaterVersion = gson.fromJson(jsonParent["updaterVersion"], CardifyUpdaterVersion::class.java)
            } catch (e: Exception) {
                throw CardifyException("Unable to parse updater version info JSON")
            }

            return CardifyVersionData(latestUpdaterVersion, latestVersion)
        } catch (e: Exception) {
            logger.error("Error getting latest version", e)
            showErrorDialog(e)
            return CardifyVersionData(CardifyUpdaterVersion("", ""), CardifyVersion("NONE", 0, "", false, "", ""))
        }
    }

    override fun start(stage: Stage) {
        System.setProperty("java.net.preferIPv4Stack", "true")
        setLoggerDir()

        val versionData = getLatestVersion()
        val updaterUI = CardifyUpdaterUI(stage, versionData)
        val scene = updaterUI.initialize()
        stage.scene = scene
        stage.show()
        stage.title = "Update to ${versionData.cardifyVersion.name}"

        updaterUI.startUpdate()
    }
}