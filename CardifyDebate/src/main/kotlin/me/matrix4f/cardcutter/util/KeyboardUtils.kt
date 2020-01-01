package me.matrix4f.cardcutter.util

import java.awt.Robot
import java.awt.Toolkit
import java.awt.event.KeyEvent

private fun copy(str: String) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(HTMLSelection(str), null)
}

private fun paste(r: Robot) {
    r.keyPress(KeyEvent.VK_CONTROL)
    r.keyPress(KeyEvent.VK_V)
    r.keyRelease(KeyEvent.VK_V)
    r.keyRelease(KeyEvent.VK_CONTROL)
}

fun pasteCardToVerbatim(html: String) {
    val r = Robot()
    r.autoDelay = 0

    copy(html)
    paste(r)
}