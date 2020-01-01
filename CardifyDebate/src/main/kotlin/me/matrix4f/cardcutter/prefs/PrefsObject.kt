package me.matrix4f.cardcutter.prefs

import me.matrix4f.cardcutter.CardCutterApplication

class PrefsObject {

    var lastUsedVersion = CardCutterApplication.CURRENT_VERSION
    var lastUsedVersionInt = CardCutterApplication.CURRENT_VERSION_INT
    
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