package me.matrix4f.cardcutter.platformspecific

import me.matrix4f.cardcutter.util.OS
import me.matrix4f.cardcutter.util.getOSType
import me.matrix4f.cardcutter.util.is32Or64
import java.util.Arrays
import java.util.function.Predicate
import java.util.stream.Collectors
import java.util.stream.Stream

class MSWordInteractor {

    /**
     * Use this API
     * @return A list of all the titles of usable MS Word windows
     */
    fun getValidWordWindows() = getWordWindows().iterator().asSequence()
        .filter { it.endsWith(" - Word") }
        .map { it.substring(0, it.length - " - Word".length) }
        .toList()


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
        return selectWordWindow("$docName - Word")
    }

    /**
     * Brings a word window into focus
     * @param title The title of the window to be brought into focus
     * @return Whether the operation was successful
     */
    external fun selectWordWindow(title: String): Boolean

    external fun setShiftKeyState(pressed: Boolean)

    companion object {
        init {
            if (getOSType() == OS.WINDOWS) {
                val processor = is32Or64();
                if (processor == 64)
                    System.loadLibrary("NativeDllInterface-x64")
                else
                    System.loadLibrary("NativeDllInterface-Win32")
            }
        }
    }

}
