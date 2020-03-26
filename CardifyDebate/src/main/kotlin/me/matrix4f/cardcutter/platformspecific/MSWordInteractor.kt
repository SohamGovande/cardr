package me.matrix4f.cardcutter.platformspecific

import me.matrix4f.cardcutter.util.OS
import me.matrix4f.cardcutter.util.getOSType
import me.matrix4f.cardcutter.util.getProcessorBits
import org.apache.logging.log4j.LogManager

class MSWordInteractor {

    /**
     * Use this API
     * @return A list of all the titles of usable MS Word windows
     */
    fun getValidWordWindows(): List<String> {
        if (LOADED_JNI)
            return getWordWindows().iterator().asSequence()
                .filter { it.endsWith(" - Word") }
                .map { it.substring(0, it.length - " - Word".length) }
                .toList()
        else
            return emptyList()
    }


    /**
     * @see getValidWordWindows
     *
     * @return A string of all windows with Win32 class L"OpusApp"
     */
    external fun getWordWindows(): Array<String>

    /**
     * Brings a word window into focus
     * @param title The title of the window to be brought into focus
     * @return Whether the operation was successful
     */
    fun selectWordWindowByDocName(docName: String): Boolean {
        if (!LOADED_JNI)
            return false
        return selectWordWindow("$docName - Word")
    }

    /**
     * Brings a word window into focus
     * @param title The title of the window to be brought into focus
     * @return Whether the operation was successful
     */
    external fun selectWordWindow(title: String): Boolean

    companion object {
        val logger = LogManager.getLogger(MSWordInteractor::class.java)
        var LOADED_JNI = true

        init {
            try {
                if (getOSType() == OS.WINDOWS) {
                    val processor = getProcessorBits()
                    if (processor == 64)
                        System.loadLibrary("NativeDllInterface-x64")
                    else
                        System.loadLibrary("NativeDllInterface-Win32")
                }
            } catch (e: Throwable) {
                LOADED_JNI = false
                logger.error("Unable to load native functions", e)
            }
        }
    }

}
