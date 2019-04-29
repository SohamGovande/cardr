package me.matrix4f.cardcutter.util

import me.matrix4f.cardcutter.card.Cite
import me.matrix4f.cardcutter.prefs.Prefs
import java.awt.Toolkit
import java.awt.Robot
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent

fun copy(str: String) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(str), null)
}

fun paste(r: Robot) {
    r.keyPress(KeyEvent.VK_CONTROL)
    r.keyPress(KeyEvent.VK_V)
    r.keyRelease(KeyEvent.VK_V)
    r.keyRelease(KeyEvent.VK_CONTROL)
}

fun pasteCardToVerbatim(tag: String, cite: Cite, body: String) {
    val r = Robot()
    r.autoDelay = 50

    copy(tag)
    r.delay(50)
    paste(r)
    r.keyPress(KeyEvent.VK_F7)
    r.keyRelease(KeyEvent.VK_F7)

    r.keyPress(KeyEvent.VK_ENTER)
    r.keyRelease(KeyEvent.VK_ENTER)

    copy(cite.getNameAndDate())
    r.keyPress(KeyEvent.VK_F8)
    r.keyRelease(KeyEvent.VK_F8)
    paste(r)

    r.keyPress(KeyEvent.VK_END)
    r.keyRelease(KeyEvent.VK_END)

    val fontName = Prefs.get().fontName
    val fontSizeEm = Prefs.get().fontSize/11.0

    val restOfPaste = """
        <span style="font-family: $fontName;font-size:'${fontSizeEm}em';">${cite.getDetailedInfo().replace(" ", "&nbsp;")}</span>
        <p style="font-family: $fontName;font-size:'${fontSizeEm}em';">${body}</p>
    """.trimIndent()
    Toolkit.getDefaultToolkit().systemClipboard.setContents(HtmlSelection(restOfPaste), null)
    paste(r)

    /*msWord.setShiftKeyState(true)
    r.delay(100)
    r.keyPress(KeyEvent.VK_END)
    r.keyRelease(KeyEvent.VK_END)
    msWord.setShiftKeyState(false)
    r.delay(100)

    r.autoDelay = 50
    r.keyPress(KeyEvent.VK_F12)
    r.keyRelease(KeyEvent.VK_F12)
    r.keyPress(KeyEvent.VK_SPACE)
    r.keyRelease(KeyEvent.VK_SPACE)

    copy(" - ${cite.getDetailedInfo()}")
    paste(r)

    r.keyPress(KeyEvent.VK_ENTER)
    r.keyRelease(KeyEvent.VK_ENTER)

    Toolkit.getDefaultToolkit().systemClipboard.setContents(HtmlSelection(body), null)
    paste(r)*/
}