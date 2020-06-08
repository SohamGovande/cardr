package me.sohamgovande.cardr.platformspecific

import me.sohamgovande.cardr.util.executeCommandBlocking
import org.apache.logging.log4j.LogManager
import java.nio.file.Paths


class MacMSWordInteractor {

    private val getWordWindowsFile = Paths.get(System.getProperty("cardr.data.dir"), "MacScripts", "getWordWindows.scpt").toFile()
    private val selectWordWindowFile = Paths.get(System.getProperty("cardr.data.dir"), "MacScripts", "selectWordWindow.scpt").toFile()
    private val pasteToWordFile = Paths.get(System.getProperty("cardr.data.dir"), "MacScripts", "pasteToWord.scpt").toFile()

    /**
     * Use this API
     * @return A list of all the titles of usable MS Word windows
     */
    fun getValidWordWindows(): List<String> {
        val list = getWordWindows()
                .filter { !it.contains("missing value") }
                .toList()
        if (list.size == 1 && list[0] == "")
            return emptyList()
        return list
    }


    fun pasteToWord() {
        val cmd = "osascript ${pasteToWordFile.canonicalPath}"
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
