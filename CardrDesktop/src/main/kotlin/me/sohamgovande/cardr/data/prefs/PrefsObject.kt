package me.sohamgovande.cardr.data.prefs

import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.core.ui.WindowDimensions
import java.awt.event.KeyEvent

data class PrefsObject(val unused: Nothing?) {

    var lastUsedVersion = CardrDesktop.CURRENT_VERSION
    var lastUsedVersionInt = CardrDesktop.CURRENT_VERSION_INT

    var lastFirstLaunchVersion = -1

    var cardFormat = DEFAULT_CARD_FORMAT
    var onlyCardYear = true
    var condense = false
    var useEtAl = true
    var capitalizeAuthors = false
    var endQualsWithComma = true

    var showParagraphBreaks = false
    var darkMode = false

    var useSlashInsteadOfDash = false

    var pastePlainText = true
    var pasteShortcut = KeyEvent.VK_F2

    var hidePastePlainTextDialog = false
    var hideCopyDialog = false
    var hideUpdateDialog = false

    var emailAddress = ""
    var accessToken = ""
    var encryptedPassword = ""

    var openHistoryWithinCardr = true

    var windowDimensions = WindowDimensions(-1024.1024, 0.0, 0.0, 0.0, false)

    fun getStylesheet(): String {
        return if (darkMode) "/styles-dark.css" else "/styles.css"
    }

    companion object {
        const val MAC_CALIBRI_FONT = "Arial"
        const val DEFAULT_CARD_FORMAT = "<html dir=\"ltr\"><head></head><body><h4><font face=\"Calibri\" size=\"4\">{Tag}</font></h4><p><font face=\"Calibri\"><b><font size=\"4\">{AuthorLastName},&nbsp;</font></b><font size=\"4\"><b>{DateShortened}</b></font>&nbsp;</font><span style=\"font-family: Calibri;\">({AuthorFullName}, {Qualifications}{DateFull}, accessed on {CurrentDate}, {Publication}, \"{Title}\", {Url})</span></p><p><font face=\"Calibri\">{CardBody}</font></p></body></html>"
    }
}
