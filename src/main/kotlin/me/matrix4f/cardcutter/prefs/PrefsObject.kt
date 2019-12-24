package me.matrix4f.cardcutter.prefs

import me.matrix4f.cardcutter.CardCutterApplication

class PrefsObject {

    var lastUsedVersion = CardCutterApplication.CURRENT_VERSION
    var lastUsedVersionInt = CardCutterApplication.CURRENT_VERSION_INT
    
    var fontName = "Calibri"
    var fontSize = 11
    var citeFormat = DEFAULT_CITE_FORMAT
    var onlyCardYear = false
    var condense = true
    var useEtAl = true
    var emailAddress = ""
    var accessToken = ""

    companion object {
        const val DEFAULT_CITE_FORMAT = " (<Author>, <Qualifications><Date>, accessed on <CurrentDate>, <Publication>, \"<Title>\", <Url>)"
    }
}