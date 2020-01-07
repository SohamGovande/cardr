package me.matrix4f.cardcutter

import javafx.application.Application
import me.matrix4f.cardcutter.ui.CardCuttingUI
import me.matrix4f.cardcutter.util.OS
import me.matrix4f.cardcutter.util.getOSType
import me.matrix4f.cardcutter.web.WebsiteCardCutter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.LoggerContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

lateinit var ui: CardCuttingUI

fun main(args: Array<String>) {
    if (args.size == 1) {
        Thread {
            val reader = WebsiteCardCutter(args[0])
            @Suppress("SENSELESS_COMPARISON")
            while (ui == null || !ui.loaded) { }
            ui.loadFromReader(reader)
        }.start()
    }
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

    Application.launch(CardifyDebate::class.java)
}