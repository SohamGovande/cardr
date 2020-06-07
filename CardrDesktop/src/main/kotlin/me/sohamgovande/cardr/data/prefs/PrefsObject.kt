package me.sohamgovande.cardr.data.prefs

import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.core.ui.WindowDimensions
import java.awt.event.KeyEvent

data class PrefsObject(
    var lastUsedVersion: String = CardrDesktop.CURRENT_VERSION,
    var lastUsedVersionInt: Int = CardrDesktop.CURRENT_VERSION_INT,

    var lastFirstLaunchVersion: Int = -1,

    var cardFormat: String = DEFAULT_CARD_FORMAT,
    var onlyCardYear: Boolean = true,
    var condense: Boolean = false,
    var useEtAl: Boolean = true,
    var capitalizeAuthors: Boolean = false,
    var endQualsWithComma: Boolean = true,

    var showParagraphBreaks: Boolean = false,
    var darkMode: Boolean = false,

    var useSlashInsteadOfDash: Boolean = false,

    var pastePlainText: Boolean = true,
    var pasteShortcut: Int = KeyEvent.VK_F2,

    var hidePastePlainTextDialog: Boolean = false,
    var hideCopyDialog: Boolean = false,
    var hideUpdateDialog: Boolean = false,
    var hideFormattingDialog: Boolean = false,

    var emailAddress: String = "",
    var accessToken: String = "",
    var encryptedPassword: String = "",

    var openHistoryWithinCardr: Boolean = true,

    var underlineShortcut: Int = KeyEvent.VK_F9,
    var emphasizeShortcut: Int = KeyEvent.VK_F10,
    var boldShortcut: Int = KeyEvent.VK_F8,
    var highlightShortcut: Int = KeyEvent.VK_F11,
    var unhighlightShortcut: Int = KeyEvent.VK_F12,
    var highlightColor: String = "#ffff00",

    var activeProperties: MutableList<Int> = mutableListOf(0, 1, 2, 3, 4, 5),

    val toolPanes: MutableMap<String, Boolean> = mutableMapOf(),

    var windowDimensions: WindowDimensions = WindowDimensions(-1024.1024, 0.0, 0.0, 0.0, false),
    var ocrWindowDimensions: WindowDimensions =
        WindowDimensions(-1024.1024, 0.0, 0.0, 0.0, false)
) {

    fun getStylesheet(): String = if (darkMode) "/styles-dark.css" else "/styles.css"

    companion object {
        val COLOR_MAP = mapOf(
            Pair("Yellow", "#ffff00"),
            Pair("Light Green", "#00ff00"),
            Pair("Light Blue", "#00ffff"),
            Pair("Magenta", "#ff00ff"),
            Pair("Red", "#ff0000"),
            Pair("Dark Blue", "#0000ff")
        )
        const val MAC_CALIBRI_FONT = "Arial"
        const val DEFAULT_CARD_FORMAT = "<html dir=\"ltr\"><head></head><body><h4><font face=\"Calibri\" size=\"4\">{Tag}</font></h4><p><font face=\"Calibri\"><b><font size=\"4\">{AuthorLastName},&nbsp;</font></b><font size=\"4\"><b>{DateShortened}</b></font>&nbsp;</font><span style=\"font-family: Calibri;\">({AuthorFullName}, {Qualifications}{DateFull}, accessed on {CurrentDate}, {Publication}, \"{Title}\", {Url})</span></p><p><font face=\"Calibri\">{CardBody}</font></p></body></html>"
    }
}
