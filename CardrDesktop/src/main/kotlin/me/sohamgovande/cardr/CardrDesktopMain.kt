package me.sohamgovande.cardr

import javafx.application.Application
import javafx.application.Platform
import me.sohamgovande.cardr.comm.CardrServerClient
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.core.ui.tabs.EditCardTabUI
import me.sohamgovande.cardr.core.ui.windows.ocr.OCRSelectionWindow
import me.sohamgovande.cardr.util.showErrorDialogUnblocking
import me.sohamgovande.cardr.core.web.WebsiteCardCutter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import java.io.BufferedReader
import java.io.File
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

var ui: CardrUI? = null
val finishedLoadingUiLock = Object()
val constructorUiLock = Object()
lateinit var LAUNCH_ARGS: Array<String>
var CHROME_OCR_MODE = false

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
    val log4j2 = CardrDesktop::class.java.getResource("/log4j2.xml")
    context.configLocation = log4j2.toURI()
    CardrDesktop.logger.info("Set logger data directory to '$dataDir'")
}

fun processArgs(args: List<String>, ui: () -> CardrUI?, nonNullCurrentTab: () -> EditCardTabUI) {
    try {
        if (args.size == 1) {
            if (args[0] == "ocr") {
                CHROME_OCR_MODE = true

                if (ui() == null) {
                    synchronized(constructorUiLock) {
                        constructorUiLock.wait()
                        Platform.runLater { OCRSelectionWindow.openWindow(ui()!!, nonNullCurrentTab) }
                    }
                } else {
                    Platform.runLater { OCRSelectionWindow.openWindow(ui()!!, nonNullCurrentTab) }
                }
            } else {
                if (ui() == null || !ui()!!.finishedDeferredLoad) {
                    finishedLoadingUiLock.wait()
                }
                val reader = WebsiteCardCutter(null, nonNullCurrentTab, args[0], null)
                nonNullCurrentTab().loadFromReader(reader)
            }
        } else if (args.size == 2) {
            try {
                val cardID = args[1]
                CardrDesktop.logger.info("Loaded card ID $cardID")

                val reader = WebsiteCardCutter(null, { ui()!!.getSelectedTab(EditCardTabUI::class.java)!! }, args[0], cardID)
                finishedLoadingUiLock.wait()
                ui()!!.getSelectedTab(EditCardTabUI::class.java)?.loadFromReader(reader)

                val selectionDataFile = Paths.get(System.getProperty("cardr.data.dir"), "CardrSelection-$cardID.txt").toFile()
                val selectionData: String = selectionDataFile.inputStream().bufferedReader().use(BufferedReader::readText)
                if (selectionData.isNotBlank()) {
                    Platform.runLater {
                        ui()!!.getSelectedTab(EditCardTabUI::class.java)?.keepOnlyText(selectionData)
                    }
                }
                if (CardrDesktop.RELEASE_MODE)
                    selectionDataFile.deleteOnExit()
            } catch (e: Exception) {
                CardrDesktop.logger.error("Error loading selected text", e)
                showErrorDialogUnblocking("Error loading selected text: ${e.message}", "Please see the log file for additional details.")
            }
        }
    } catch (e: Exception) {
        CardrDesktop.logger.error("Error preloading page", e)
    }
}

fun main(args: Array<String>) {
    LAUNCH_ARGS = args
    val useArgs = args.toList().filter { !it.startsWith("cardrOption") }
    System.setProperty("java.net.preferIPv4Stack", "true")

    setLoggerDir()

    Thread {
        if (CardrServerClient.checkExistingServers(useArgs)) {
            exitProcess(0)
        }

        CardrServerClient.createNewServer()
    }.start()

    if (useArgs.isNotEmpty()) {
        Thread {
            synchronized(finishedLoadingUiLock) {
                processArgs(useArgs, { ui }, {
                    val nullableTab = ui!!.getSelectedTab(EditCardTabUI::class.java)
                    return@processArgs nullableTab!!
                })
            }
        }.start()
    }

    CardrDesktop.logger.info("Launching Cardr with the following arguments: ${args.contentToString()}")
    CardrDesktop.logger.info("Running on Java version ${System.getProperty("java.version")} by vendor ${System.getProperty("java.vendor")}")

    Application.launch(CardrDesktop::class.java)
}
