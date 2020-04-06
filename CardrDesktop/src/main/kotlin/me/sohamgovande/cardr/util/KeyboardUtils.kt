package me.sohamgovande.cardr.util

import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.platformspecific.MacMSWordInteractor
import java.awt.Robot
import java.awt.Toolkit
import java.awt.event.KeyEvent

enum class KeyboardPasteMode {
    NORMAL, PLAIN_TEXT
}

private fun copy(str: String) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(HTMLSelection(str), null)
}

fun pasteObject(data: String, pasteMode: KeyboardPasteMode) {
    if (getOSType() == OS.MAC) {
        copy(data)
        if (pasteMode == KeyboardPasteMode.NORMAL) {
            MacMSWordInteractor().pasteToWord()
        } else {
            val r = Robot()
            r.autoDelay = 0

            copy(data)
            r.keyPress(Prefs.get().pasteShortcut)
            r.keyRelease(Prefs.get().pasteShortcut)
            r.delay(500)
        }
        return
    } else {
        val r = Robot()
        r.autoDelay = 0

        copy(data)
        if (pasteMode == KeyboardPasteMode.NORMAL) {
            val ctrlKey = KeyEvent.VK_CONTROL
            r.keyPress(ctrlKey)
            r.keyPress(KeyEvent.VK_V)
            r.keyRelease(KeyEvent.VK_V)
            r.keyRelease(ctrlKey)
        } else if (pasteMode == KeyboardPasteMode.PLAIN_TEXT) {
            r.keyPress(Prefs.get().pasteShortcut)
            r.keyRelease(Prefs.get().pasteShortcut)
        }
        r.delay(500)
    }
}
