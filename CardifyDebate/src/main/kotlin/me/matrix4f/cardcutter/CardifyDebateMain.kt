package me.matrix4f.cardcutter

import javafx.application.Application
import javafx.application.Platform
import me.matrix4f.cardcutter.ui.CardCuttingUI
import me.matrix4f.cardcutter.updater.UpdateChecker
import me.matrix4f.cardcutter.util.OS
import me.matrix4f.cardcutter.util.getOSType
import me.matrix4f.cardcutter.util.showErrorDialog
import me.matrix4f.cardcutter.web.WebsiteCardCutter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import java.io.BufferedReader
import java.io.File
import java.nio.file.FileAlreadyExistsException
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
        try { Files.createDirectories(dataDirFileExt.parent) } catch (e: FileAlreadyExistsException) { }
        dataDir = dataDirFileExt.parent.toFile().canonicalPath + File.separator
    }
    System.setProperty("cardifydebate.data.dir", dataDir)
    (LogManager.getContext(false) as LoggerContext).configLocation = CardifyDebate::class.java.getResource("/log4j2.xml").toURI()
    CardifyDebate.logger.info("Set logger data directory to '$dataDir'")
}

fun main(args: Array<String>) {
    System.setProperty("java.net.preferIPv4Stack", "true")

    if (args.size >= 1) {
        Thread {
            try {
                synchronized(uiLock) {
                    try {
                        if (args.size == 1) {
                            val reader = WebsiteCardCutter(args[0], null)
                            uiLock.wait()
                            ui!!.loadFromReader(reader)
                        } else if (args.size == 2) {
                            val cardID = args[1]
                            CardifyDebate.logger.info("Loaded card ID $cardID")

                            val reader = WebsiteCardCutter(args[0], cardID)
                            uiLock.wait()
                            ui!!.loadFromReader(reader)

                            val selectionDataFile = Paths.get(System.getProperty("cardifydebate.data.dir"), "CardifySelection-$cardID.txt").toFile()
                            val selectionData: String = selectionDataFile.inputStream().bufferedReader().use(BufferedReader::readText)
                            if (selectionData.isNotBlank()) {
                                Platform.runLater {
                                    ui!!.keepOnlyText(selectionData)
                                }
                            }
                            if (CardifyDebate.RELEASE_MODE)
                                selectionDataFile.deleteOnExit()
                        }
                    } catch (e: Exception) {
                        showErrorDialog("Error loading selected text: ${e.message}", "Please see the log file for additional details.")
                        CardifyDebate.logger.error("Error loading selected text", e)
                    }
                }
            } catch (e: Exception) {
                CardifyDebate.logger.error("Error preloading page", e)
            }
        }.start()
    }

    setLoggerDir()

    CardifyDebate.logger.info("Launching Cardify with the following arguments: ${Arrays.toString(args)}")

    Application.launch(CardifyDebate::class.java)
}