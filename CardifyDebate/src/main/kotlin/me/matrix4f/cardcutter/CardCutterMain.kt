package me.matrix4f.cardcutter

import javafx.application.Application
import me.matrix4f.cardcutter.ui.CardCuttingUI
import me.matrix4f.cardcutter.updater.UpdateChecker
import me.matrix4f.cardcutter.util.OS
import me.matrix4f.cardcutter.util.getOSType
import me.matrix4f.cardcutter.web.WebsiteCardCutter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

var ui: CardCuttingUI? = null
val uiLock = Object()

private fun setLoggerDir() {
    var dataDir = ""
    if (getOSType() == OS.MAC) {
        println("Detected macOS, changing data directory...")
        val dataDirFileExt = Paths.get(
            System.getProperty("user.home"), "CardifyDebate", "test.txt"
        )
        Files.createDirectories(dataDirFileExt.parent)
        dataDir = dataDirFileExt.parent.toFile().canonicalPath + File.separator
    }
    System.setProperty("cardifydebate.data.dir", dataDir)
    (LogManager.getContext(false) as LoggerContext).configLocation = CardifyDebate::class.java.getResource("/log4j2.xml").toURI()
    CardifyDebate.logger.info("Set logger data directory to '$dataDir'")
}

private fun checkForUpdates() {
    Thread {
        val updateChecker = UpdateChecker()
        updateChecker.checkForUpdates()
    }.start()
}

fun main(args: Array<String>) {
    if (args.size >= 1) {
        Thread {
            try {
                synchronized(uiLock) {
                    val reader = WebsiteCardCutter(args[0])
                    uiLock.wait()
                    ui!!.loadFromReader(reader)
                }
            } catch (e: Exception) {
                CardifyDebate.logger.error("Error preloading page", e)
            }
        }.start()
    }

    setLoggerDir()
    System.setProperty("java.net.preferIPv4Stack", "true")

    CardifyDebate.logger.info("Launching Cardify with the following arguments: ${Arrays.toString(args)}")

    checkForUpdates()

    Application.launch(CardifyDebate::class.java)
}