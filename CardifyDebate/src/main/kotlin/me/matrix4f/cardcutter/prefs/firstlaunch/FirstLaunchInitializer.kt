package me.matrix4f.cardcutter.prefs.firstlaunch
import me.matrix4f.cardcutter.prefs.Prefs
import me.matrix4f.cardcutter.prefs.PrefsObject
import me.matrix4f.cardcutter.util.*
import net.lingala.zip4j.ZipFile
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.LogManager
import java.io.File
import java.net.URL
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Paths

private val logger = LogManager.getLogger(Prefs::class.java)

@Throws(FirstLaunchException::class, Exception::class)
private fun downloadChromeDataWindows(): File {
    executeCommandBlocking("taskkill /f /im CardifyChromeApp.exe", logger, true)

    val executablePath = Paths.get("extension", "test.txt").parent
    try { Files.createDirectories(executablePath) } catch (e: FileAlreadyExistsException) { }

    val executable = Paths.get("extension", "CardifyChromeApp.exe").toFile()
    downloadFileFromURL("http://cardifydebate.x10.bz/data/win/CardifyChromeApp-v1.2.0.exe", executable, logger)

    val jsonFile = Paths.get("extension", "me.matrix4f.cardify.json").toFile()
    downloadFileFromURL("http://cardifydebate.x10.bz/data/win/ChromeAppNativeJson-v1.2.0.json", jsonFile, logger)

    if (!jsonFile.exists())
        throw FirstLaunchException("Unable to download Chrome App Native JSON.")

    return jsonFile
}

@Throws(FirstLaunchException::class, Exception::class)
private fun downloadChromeDataMacOS() {
    logger.info("Entered macOS First Launch")
    logger.info("Creating paths...")
    val jsonPath = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Google", "Chrome", "NativeMessagingHosts", "me.matrix4f.cardify.json")
    if (!jsonPath.parent.parent.toFile().exists())
        throw FirstLaunchException("No Google Chrome installation detected")

    val executablePath = Paths.get(System.getProperty("cardifydebate.data.dir"), "CardifyChromeApp")
    val executableZipPath = Paths.get(System.getProperty("cardifydebate.data.dir"), "CardifyChromeApp.zip")
    try { Files.createDirectories(executablePath.parent) } catch (e: FileAlreadyExistsException) { }

    logger.info("Opening data stream for json file...")
    val dataStream = URL("http://cardifydebate.x10.bz/data/mac/ChromeAppNativeJson-v1.2.0.json").openStream()

    logger.info("Reading json file...")
    @Suppress("DEPRECATION") var data = IOUtils.toString(dataStream)
    IOUtils.closeQuietly(dataStream)

    logger.info("Writing json file to ${jsonPath.toFile().absolutePath} with executable ${executablePath.toFile().absolutePath}...")
    data = data.replace("%FILEPATH%", executablePath.toFile().absolutePath)
    Files.write(jsonPath, data.toByteArray())

    downloadFileFromURL("http://cardifydebate.x10.bz/data/mac/CardifyChromeApp-v1.2.0.zip", executableZipPath.toFile(), logger)

    logger.info("Extracting zipped executable...")
    val zipFile = ZipFile(executableZipPath.toFile())
    zipFile.extractAll(executablePath.parent.toFile().absolutePath)
    executableZipPath.toFile().deleteOnExit()

    makeFileExecutableViaChmod(executablePath.toFile().absolutePath, logger)

    if (!jsonPath.toFile().exists())
        throw FirstLaunchException("Unable to download Chrome App Native JSON.")
    if (!executablePath.toFile().exists())
        throw FirstLaunchException("Unable to download Chrome App Native Executable.")
}

@Throws(FirstLaunchException::class, Exception::class)
private fun onFirstLaunchWindows() {
    val jsonFile = downloadChromeDataWindows()

    val commands = arrayOf(
        "REG DELETE \"HKCU\\Software\\Google\\Chrome\\NativeMessagingHosts\\me.matrix4f.cardify\" /f",
        "REG DELETE \"HKLM\\Software\\Google\\Chrome\\NativeMessagingHosts\\me.matrix4f.cardify\" /f",
        "REG ADD \"HKCU\\Software\\Google\\Chrome\\NativeMessagingHosts\\me.matrix4f.cardify\" /ve /t REG_SZ /d \"${jsonFile.absolutePath}\" /f"
    )
    for (cmd in commands) {
        executeCommandBlocking(cmd, logger, true)
    }
}

@Throws(FirstLaunchException::class, Exception::class)
private fun onFirstLaunchMacOS() {
    downloadChromeDataMacOS()

    val macScriptsPath = Paths.get(System.getProperty("cardifydebate.data.dir"), "MacScripts")
    try { Files.createDirectory(macScriptsPath) } catch (e: FileAlreadyExistsException) { }

    val getWordWindowsScriptPath = Paths.get(macScriptsPath.toFile().absolutePath, "getWordWindows.scpt")
    val selectWordWindowScriptPath = Paths.get(macScriptsPath.toFile().absolutePath, "selectWordWindow.scpt")

    downloadFileFromURL("http://cardifydebate.x10.bz/data/mac/MacScripts/getWordWindows.scpt", getWordWindowsScriptPath.toFile(), logger)
    downloadFileFromURL("http://cardifydebate.x10.bz/data/mac/MacScripts/selectWordWindow.scpt", selectWordWindowScriptPath.toFile(), logger)

    if (!selectWordWindowScriptPath.toFile().exists())
        throw FirstLaunchException("Unable to download AppleScript 'selectWordWindow'.")
    if (!getWordWindowsScriptPath.toFile().exists())
        throw FirstLaunchException("Unable to download AppleScript 'getWordWindows'.")
}

fun onFirstLaunch(): Exception? {
    return try {
        logger.info("First launch method invoked with OS ${System.getProperty("os.name")}")
        if (getOSType() == OS.WINDOWS)
            onFirstLaunchWindows()
        if (getOSType() == OS.MAC)
            onFirstLaunchMacOS()
        null
    } catch (e: Exception) {
        e
    }
}

fun updateFrom(from: Int, to: Int): Exception? {
    if (from == 1 && to >= 2) {
        logger.info("Resetting card format...")
        val prefs = Prefs.get()
        prefs.cardFormat = PrefsObject.DEFAULT_CARD_FORMAT
        if (getOSType() == OS.MAC) {
            prefs.cardFormat = prefs.cardFormat.replace("Calibri", PrefsObject.MAC_CALIBRI_FONT)
        }
    }
    if (from < 3 && to >= 3) {
        logger.info("Updating CardifyChromeApp")
        if (getOSType() == OS.MAC) {
            downloadChromeDataMacOS()
        } else {
            downloadChromeDataWindows()
        }
    }
    return null
}
