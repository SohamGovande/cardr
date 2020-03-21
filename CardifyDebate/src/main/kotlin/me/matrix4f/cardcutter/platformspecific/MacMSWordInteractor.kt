package me.matrix4f.cardcutter.platformspecific

import me.matrix4f.cardcutter.util.executeCommandBlocking
import org.apache.logging.log4j.LogManager
import java.nio.file.Paths


class MacMSWordInteractor {

    val getWordWindowsFile = Paths.get(System.getProperty("cardifydebate.data.dir"), "MacScripts", "getWordWindows.scpt").toFile()
    val selectWordWindowFile = Paths.get(System.getProperty("cardifydebate.data.dir"), "MacScripts", "selectWordWindow.scpt").toFile()

    /**
     * Use this API
     * @return A list of all the titles of usable MS Word windows
     */
    fun getValidWordWindows() =
        getWordWindows()
            .filter { it != "missing value" }
            .toList()


    /**
     * @see getValidWordWindows
     *
     * @return A string of all windows with Win32 class L"OpusApp"
     */
    fun getWordWindows(): Array<String> {
        val cmd = "osascript ${getWordWindowsFile.canonicalPath}"
        val ret  = executeCommandBlocking(cmd, logger).split(", ")
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
        executeCommandBlocking(cmd, logger)
        return true
    }

    companion object {
        val logger = LogManager.getLogger(MacMSWordInteractor::class.java)
    }
}
