package me.sohamgovande.cardr.launcher

import javafx.application.Application
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import java.io.File
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

var programArguments: Array<String> = arrayOf()

private fun setLoggerDir() {
    val dataDir: String
    println("Changing data directory...")
    val dataDirFileExt = Paths.get(
        System.getProperty("user.home"), "cardr", "test.txt"
    )
    try { Files.createDirectories(dataDirFileExt.parent) } catch (e: FileAlreadyExistsException) { }
    dataDir = dataDirFileExt.parent.toFile().canonicalPath + File.separator
    System.setProperty("cardr.data.dir", dataDir)
    val context = (LogManager.getContext(true) as LoggerContext)
    val log4j2 = CardrLauncher::class.java.getResource("/log4j2.xml")
    context.configLocation = log4j2.toURI()
    CardrLauncher.logger.info("Set logger data directory to '$dataDir'")
}

fun main(args: Array<String>) {
    setLoggerDir()
    programArguments = args
    CardrLauncher.logger.info("Launched with arguments ${Arrays.toString(args)}")
    Application.launch(CardrLauncher::class.java)
}