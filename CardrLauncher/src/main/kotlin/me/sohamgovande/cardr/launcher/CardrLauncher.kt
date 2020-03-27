package me.sohamgovande.cardr.launcher

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.stage.Stage
import me.sohamgovande.cardr.launcher.updater.CardrVersion
import me.sohamgovande.cardr.launcher.updater.CardrVersionData
import me.sohamgovande.cardr.launcher.util.*
import org.apache.logging.log4j.LogManager
import java.nio.file.Paths
import kotlin.system.exitProcess


class CardrLauncher : Application() {

    private lateinit var stage: Stage
    private val cardifyUpdaterPath = Paths.get(System.getProperty("cardr.data.dir"), "Cardr.jar")

    private fun getLatestVersion(): CardrVersionData {
        val logger = LogManager.getLogger(CardrLauncher::class.java)
        try {
            val data = makeRequest(UrlHelper.get("versionInfo"))
            val gson = GsonBuilder().setPrettyPrinting().setLenient().create()
            val jsonParent = JsonParser().parse(data) as JsonObject
            val latestVersion: CardrVersion
            try {
                latestVersion = gson.fromJson(jsonParent["latestVersion"], CardrVersion::class.java)
            } catch (e: Exception) {
                throw CardrException("Unable to parse version info JSON")
            }

            return CardrVersionData(latestVersion)
        } catch (e: Exception) {
            logger.error("Error getting latest version", e)
            showErrorDialog(e)
            return CardrVersionData(CardrVersion("NONE", 0, "", "", ""))
        }
    }

    private fun getCurrentChecksum(): String {
        val cardifyUpdaterFile = cardifyUpdaterPath.toFile()

        var readSha256 = ""
        if (cardifyUpdaterFile.exists()) {
            readSha256 = sha256File(cardifyUpdaterPath)
        }
        return readSha256
    }

    fun launchCardr(suppressArgs: Boolean) {
        val javaExe: String
        val dllFolderPath = Paths.get(System.getProperty("user.home"), "AppData", "Local", "CardrLauncher", "dlls")
        if (RELEASE_MODE) {
            javaExe = "\"runtime/bin/javaw.exe\""
        } else {
            javaExe = "javaw"
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
        this.stage = stage
        System.setProperty("java.net.preferIPv4Stack", "true")

        Thread {
            val versionData = getLatestVersion()
            val currentSha256 = getCurrentChecksum()
            logger.info("Read sha256 '$currentSha256'")
            logger.info("Latest version data: $versionData")
            if (versionData.cardrVersion.name == "NONE" || currentSha256 != versionData.cardrVersion.sha256) {
                Platform.runLater {
                    val updaterUI = UpdaterUI(stage, versionData, this)
                    stage.scene = updaterUI.initialize()
                    updaterUI.startUpdate()
                }
            } else {
                launchCardr(false)
            }
        }.start()

        val loadingUI = LoadingUI()
        stage.scene = loadingUI.initialize()
        stage.title = "Launcher"
        stage.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))
        stage.show()
    }

    companion object {
        val RELEASE_MODE = false
        val logger = LogManager.getLogger(CardrLauncher::class.java)
    }
}