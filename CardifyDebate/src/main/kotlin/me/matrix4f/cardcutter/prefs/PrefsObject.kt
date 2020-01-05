package me.matrix4f.cardcutter.prefs

import me.matrix4f.cardcutter.CardifyDebate

class PrefsObject {

    var lastUsedVersion = CardifyDebate.CURRENT_VERSION
    var lastUsedVersionInt = CardifyDebate.CURRENT_VERSION_INT

    var lastFirstLaunchVersion = -1

    var cardFormat = DEFAULT_CARD_FORMAT
    var onlyCardYear = true
    var condense = true
    var useEtAl = true
    var emailAddress = ""
    var accessToken = ""

    companion object {
        const val DEFAULT_CARD_FORMAT = "<html dir=\"ltr\"><head></head><body ><h4><font face=\"Calibri\" size=\"4\">{Tag}</font></h4><p><font face=\"Calibri\"><b><font size=\"4\">{AuthorLastName},&nbsp;</font></b><font size=\"4\"><b>{DateShortened}</b></font>&nbsp;</font><span style=\"font-family: Calibri;\">({AuthorFullName}, {Qualifications}{DateFull}, accessed on {CurrentDate}, {Publication}, \"{Title}\", {Url})</span></p><p><font face=\"Calibri\">{CardBody}</font></p></body></html>"
    }
}