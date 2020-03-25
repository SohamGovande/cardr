package me.matrix4f.cardcutter

import javafx.application.Application
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import java.io.File
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Paths

fun setLoggerDir() {
    val dataDir: String
    println("Changing data directory...")
    val dataDirFileExt = Paths.get(
        System.getProperty("user.home"), "CardifyDebate", "test.txt"
    )
    try { Files.createDirectories(dataDirFileExt.parent) } catch (e: FileAlreadyExistsException) { }
    dataDir = dataDirFileExt.parent.toFile().canonicalPath + File.separator
    System.setProperty("cardifydebate.data.dir", dataDir)
    val context = (LogManager.getContext(true) as LoggerContext)
    val log4j2 = CardifyUpdater::class.java.getResource("/log4j2.xml")
    context.configLocation = log4j2.toURI()
}

fun main(args: Array<String>) {
    Application.launch(CardifyUpdater::class.java)
}