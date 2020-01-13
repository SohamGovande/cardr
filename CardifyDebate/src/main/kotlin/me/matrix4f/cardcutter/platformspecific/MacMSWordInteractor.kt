package me.matrix4f.cardcutter.platformspecific

import me.matrix4f.cardcutter.util.OS
import me.matrix4f.cardcutter.util.getOSType
import me.matrix4f.cardcutter.util.getProcessorBits
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.PumpStreamHandler
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStreamReader


class MacMSWordInteractor {

    /**
     * Use this API
     * @return A list of all the titles of usable MS Word windows
     */
    fun getValidWordWindows() = getWordWindows().iterator().asSequence()
//        .filter { it.endsWith(" - Word") }
//        .map { it.substring(0, it.length - " - Word".length) }
        .toList()


    /**
     * @see getValidWordWindows
     *
     * @return A string of all windows with Win32 class L"OpusApp"
     */
    fun getWordWindows(): Array<String> {
        val cmd = "osascript MacScripts/getWordWindows.scpt"
//        val cmd = "echo test, test2"
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
        System.out.println("Command '$cmd' returned '$result'")
        return result
    }
    /**
     * Brings a word window into focus
     * @param title The title of the window to be brought into focus
     * @return Whether the operation was successful
     */
    fun selectWordWindow(title: String): Boolean {
       // val cmd = "name=$title osascript src/main/kotlin/me/matrix4f/cardcutter/platformspecific/selectWordWindow.scpt"
//        val cmd = "name=$title open src/main/kotlin/me/matrix4f/cardcutter/platformspecific/selectWordWindow.app"
        System.out.println(title)
        val cmd = "osascript MacScripts/selectWordWindow.scpt \"$title\""
        runCommand(cmd)
//
        return true
    }

    external fun setShiftKeyState(pressed: Boolean)

    companion object {
        init {
            if (getOSType() == OS.WINDOWS) {
                val processor = getProcessorBits();
                if (processor == 64)
                    System.loadLibrary("NativeDllInterface-x64")
                else
                    System.loadLibrary("NativeDllInterface-Win32")
            }
        }
    }

}
