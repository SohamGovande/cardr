package me.matrix4f.cardcutter.launcher

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import javafx.application.Application
import javafx.stage.Stage
import me.matrix4f.cardcutter.launcher.updater.CardifyVersion
import me.matrix4f.cardcutter.launcher.updater.CardifyVersionData
import me.matrix4f.cardcutter.launcher.util.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import java.io.File
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class CardifyLauncher : Application() {

    private val cardifyUpdaterPath = Paths.get(System.getProperty("cardifydebate.data.dir"), "CardifyDebate.jar")

    private fun getLatestVersion(): CardifyVersionData {
        val logger = LogManager.getLogger(CardifyLauncher::class.java)
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

            return CardifyVersionData(latestVersion)
        } catch (e: Exception) {
            logger.error("Error getting latest version", e)
            showErrorDialog(e)
            return CardifyVersionData(CardifyVersion("NONE", 0, "", "", ""))
        }
    }

    private fun getCurrentCardifyChecksum(): String {
        val cardifyUpdaterFile = cardifyUpdaterPath.toFile()

        var readSha256 = ""
        if (cardifyUpdaterFile.exists()) {
            readSha256 = Hash.SHA256.checksum(cardifyUpdaterFile)!!
        }
        return readSha256
    }

    fun launchCardify(suppressArgs: Boolean) {
        val javaExe: String
        val dllFolderPath = Paths.get("dlls").toAbsolutePath()
        if (RELEASE_MODE) {
            javaExe = "\"runtime/bin/java.exe\""
        } else {
            javaExe = "java"
        }

        var fullArgs = ""
        if (!suppressArgs) {
            for (arg in programArguments) {
                fullArgs += " \"${arg}\""
            }
        }

        val cmd = "$javaExe " +
            "-Djava.library.path=\"${dllFolderPath.toFile().canonicalPath}\" " +
            "-jar \"${cardifyUpdaterPath.toFile().canonicalPath}\"" +
            fullArgs
        executeCommandUnblocking(cmd, logger)
        exitProcess(0)
    }

    override fun start(stage: Stage) {
        System.setProperty("java.net.preferIPv4Stack", "true")

        val versionData = getLatestVersion()
        val currentSha256 = getCurrentCardifyChecksum()
        logger.info("Read sha256 '$currentSha256'")
        logger.info("Latest version data: $versionData")
        if (versionData.cardifyVersion.name == "NONE" || currentSha256 != versionData.cardifyVersion.sha256) {
            val updaterUI = UpdaterUI(stage, versionData, this)
            val scene = updaterUI.initialize()
            stage.scene = scene
            stage.show()
            stage.title = "CardifyDebate Launcher"

            updaterUI.startUpdate()
        } else {
            launchCardify(false)
        }
    }

    companion object {
        val RELEASE_MODE = true
        val logger = LogManager.getLogger(CardifyLauncher::class.java)
    }
}