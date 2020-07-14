package me.sohamgovande.cardr.platformspecific

import me.sohamgovande.cardr.util.executeCommandBlocking
import org.apache.logging.log4j.LogManager
import java.awt.Desktop
import java.io.File
import java.nio.file.Paths


class MacMSWordInteractor {

    private val getWordWindowsFile = Paths.get(System.getProperty("cardr.data.dir"), "MacScripts", "getWordWindows.scpt").toFile()
    private val selectWordWindowFile = Paths.get(System.getProperty("cardr.data.dir"), "MacScripts", "selectWordWindow.scpt").toFile()
    private val pasteToWordFile = Paths.get(System.getProperty("cardr.data.dir"), "MacScripts", "pasteToWord.scpt").toFile()
    private val openWordFile = Paths.get(System.getProperty("cardr.data.dir"), "MacScripts", "openWord.scpt").toFile()
    private val copyDependenciesFile = Paths.get(System.getProperty("cardr.data.dir"), "MacScripts", "copyOCRDependencies.scpt").toFile()

    fun createNewDoc() {
        executeCommandBlocking("osascript ${openWordFile.canonicalPath}", logger, true)
    }

    /**
     * Use this API
     * @return A list of all the titles of usable MS Word windows
     */
    fun getValidWordWindows(): List<String> {
        return getWordWindows()
                .filter { !it.contains("missing value") && !it.isBlank()}
                .toList()
    }


    fun pasteToWord() {
        val cmd = "osascript ${pasteToWordFile.canonicalPath}"
        executeCommandBlocking(cmd, logger, true)
    }

    fun copyOCRDependencies() {
        val cmd = "osascript ${copyDependenciesFile.canonicalPath}"
        executeCommandBlocking(cmd, logger, true)
    }

    /**
     * @see getValidWordWindows
     *
     * @return A string of all windows with Win32 class L"OpusApp"
     */
    fun getWordWindows(): Array<String> {
        val cmd = "osascript ${getWordWindowsFile.canonicalPath}"
        val ret  = executeCommandBlocking(cmd, logger, true).split(", ")
        return ret.toTypedArray()
    }

    /**
     * Brings a word window into focus
     * @param title The title of the window to be brought into focus
     * @return Whether the operation was successful
     */
    fun selectWordWindowByDocName(docName: String): Boolean {
        return selectWordWindow(docName)
    }

    /**
     * Brings a word window into focus
     * @param title The title of the window to be brought into focus
     * @return Whether the operation was successful
     */
    fun selectWordWindow(title: String): Boolean {
        val cmd = "osascript ${selectWordWindowFile.canonicalPath} \"$title\""
        logger.info("Selecting word window $title")
        executeCommandBlocking(cmd, logger, true)
        return true
    }

    companion object {
        val logger = LogManager.getLogger(MacMSWordInteractor::class.java)
    }
}
