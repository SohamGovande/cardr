package me.matrix4f.cardcutter.data.prefs

import me.matrix4f.cardcutter.CardifyDebate
import me.matrix4f.cardcutter.ui.WindowDimensions
import java.awt.event.KeyEvent

class PrefsObject {

    var lastUsedVersion = CardifyDebate.CURRENT_VERSION
    var lastUsedVersionInt = CardifyDebate.CURRENT_VERSION_INT

    var lastFirstLaunchVersion = -1

    var cardFormat = DEFAULT_CARD_FORMAT
    var onlyCardYear = true
    var condense = false
    var useEtAl = true
    var capitalizeAuthors = false
    var endQualsWithComma = true

    var showParagraphBreaks = false
    var darkMode = false

    var pastePlainText = true
    var pasteShortcut = KeyEvent.VK_F2

    var emailAddress = ""
    var accessToken = ""
    var encryptedPassword = ""

    var windowDimensions = WindowDimensions(-1024.1024, 0.0, 0.0, 0.0, false)

    fun getStylesheet(): String {
        return if (darkMode) "/styles-dark.css" else "/styles.css"
    }

    companion object {
        const val MAC_CALIBRI_FONT = "Helvetica"
        const val DEFAULT_CARD_FORMAT = "<html dir=\"ltr\"><head></head><body><h4><font face=\"Calibri\" size=\"4\">{Tag}</font></h4><p><font face=\"Calibri\"><b><font size=\"4\">{AuthorLastName},&nbsp;</font></b><font size=\"4\"><b>{DateShortened}</b></font>&nbsp;</font><span style=\"font-family: Calibri;\">({AuthorFullName}, {Qualifications}{DateFull}, accessed on {CurrentDate}, {Publication}, \"{Title}\", {Url})</span></p><p><font face=\"Calibri\">{CardBody}</font></p></body></html>"
    }

    override fun toString(): String {
        return "PrefsObject(lastUsedVersion='$lastUsedVersion', lastUsedVersionInt=$lastUsedVersionInt, lastFirstLaunchVersion=$lastFirstLaunchVersion, cardFormat='$cardFormat', onlyCardYear=$onlyCardYear, condense=$condense, useEtAl=$useEtAl, endQualsWithComma=$endQualsWithComma, emailAddress='$emailAddress', accessToken='$accessToken')"
    }


}