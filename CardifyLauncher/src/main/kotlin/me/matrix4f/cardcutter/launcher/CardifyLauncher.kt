package me.matrix4f.cardcutter.launcher

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.stage.Stage
import me.matrix4f.cardcutter.launcher.updater.CardifyVersion
import me.matrix4f.cardcutter.launcher.updater.CardifyVersionData
import me.matrix4f.cardcutter.launcher.util.*
import org.apache.logging.log4j.LogManager
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Paths
import kotlin.system.exitProcess


class CardifyLauncher : Application() {

    private lateinit var stage: Stage
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
            readSha256 = sha256File(cardifyUpdaterPath)
        }
        return readSha256
    }

    fun launchCardify(suppressArgs: Boolean) {
//        val child = URLClassLoader(arrayOf<URL>(cardifyUpdaterPath.toFile().toURI().toURL()),
//            this.javaClass.classLoader
//        )
//        val classToLoad = Class.forName("me.matrix4f.cardcutter.CardifyDebate", true, child)
//        val method: Method = classToLoad.getDeclaredMethod("start", Stage::class.java)
//        val instance = classToLoad.newInstance()
//        Platform.runLater { method.invoke(instance, stage) }

        val javaExe: String
        val dllFolderPath = Paths.get(System.getProperty("user.home"), "AppData", "Local", "CardifyLauncher", "dlls")
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
            val currentSha256 = getCurrentCardifyChecksum()
            logger.info("Read sha256 '$currentSha256'")
            logger.info("Latest version data: $versionData")
            if (versionData.cardifyVersion.name == "NONE" || currentSha256 != versionData.cardifyVersion.sha256) {
                Platform.runLater {
                    val updaterUI = UpdaterUI(stage, versionData, this)
                    stage.scene = updaterUI.initialize()
                    updaterUI.startUpdate()
                }
            } else {
                launchCardify(false)
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
        val logger = LogManager.getLogger(CardifyLauncher::class.java)
    }
}