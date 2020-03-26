package me.matrix4f.cardcutter.launcher

import javafx.application.Application
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import java.io.File
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Paths

var programArguments: Array<String> = arrayOf()

private fun setLoggerDir() {
    val dataDir: String
    println("Changing data directory...")
    val dataDirFileExt = Paths.get(
        System.getProperty("user.home"), "CardifyDebate", "test.txt"
    )
    try { Files.createDirectories(dataDirFileExt.parent) } catch (e: FileAlreadyExistsException) { }
    dataDir = dataDirFileExt.parent.toFile().canonicalPath + File.separator
    System.setProperty("cardifydebate.data.dir", dataDir)
    val context = (LogManager.getContext(true) as LoggerContext)
    val log4j2 = CardifyLauncher::class.java.getResource("/log4j2.xml")
    context.configLocation = log4j2.toURI()
    CardifyLauncher.logger.info("Set logger data directory to '$dataDir'")
}

fun main(args: Array<String>) {
    setLoggerDir()
    programArguments = args
    Application.launch(CardifyLauncher::class.java)
}