package me.matrix4f.cardcutter.platformspecific

import me.matrix4f.cardcutter.util.OS
import me.matrix4f.cardcutter.util.getOSType
import me.matrix4f.cardcutter.util.getProcessorBits
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.PumpStreamHandler
import org.apache.logging.log4j.LogManager
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStreamReader


class MacMSWordInteractor {

    /**
     * Use this API
     * @return A list of all the titles of usable MS Word windows
     */
    fun getValidWordWindows() = getWordWindows().toList()


    /**
     * @see getValidWordWindows
     *
     * @return A string of all windows with Win32 class L"OpusApp"
     */
    fun getWordWindows(): Array<String> {
        val cmd = "osascript MacScripts/getWordWindows.scpt"
        val ret  = runCommand(cmd).split(", ")
        return ret.toTypedArray();
    }

    /**
     * Brings a word window into focus
     * @param title The title of the window to be brought into focus
     * @return Whether the operation was successful
     */
    fun selectWordWindowByDocName(docName: String): Boolean {
        return selectWordWindow(docName)
    }

    fun runCommand(cmd: String): String {
        val stdout = ByteArrayOutputStream()

        val stdoutPsh = PumpStreamHandler(stdout)
        val cmdLine = CommandLine.parse(cmd)
        val executor = DefaultExecutor()
        executor.streamHandler = stdoutPsh
        try {
            executor.execute(cmdLine)
        } catch (e: Exception) {
        }

        val result = stdout.toString().replace("\n", "")
        logger.info("Command '$cmd' returned '$result'")
        return result
    }
    /**
     * Brings a word window into focus
     * @param title The title of the window to be brought into focus
     * @return Whether the operation was successful
     */
    fun selectWordWindow(title: String): Boolean {
        val cmd = "osascript MacScripts/selectWordWindow.scpt \"$title\""
        logger.info("Selecting word window $title using command '$cmd'")
        runCommand(cmd)
        return true
    }

    companion object {
        val logger = LogManager.getLogger(MacMSWordInteractor::class.java)
    }
}
