package me.matrix4f.cardcutter.util

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
    val r = Robot()
    r.autoDelay = 0

    copy(data)
    if (pasteMode == KeyboardPasteMode.NORMAL) {
        r.keyPress(KeyEvent.VK_CONTROL)
        r.keyPress(KeyEvent.VK_V)
        r.keyRelease(KeyEvent.VK_V)
        r.keyRelease(KeyEvent.VK_CONTROL)
    } else if (pasteMode == KeyboardPasteMode.PLAIN_TEXT) {
        r.keyPress(KeyEvent.VK_F2)
        r.keyRelease(KeyEvent.VK_F2)
    }
    r.delay(500)
}