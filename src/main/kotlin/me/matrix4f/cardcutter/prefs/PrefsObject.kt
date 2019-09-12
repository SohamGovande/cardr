package me.matrix4f.cardcutter.prefs

class PrefsObject {

    var fontName = "Calibri"
    var fontSize = 11
    var citeFormat = DEFAULT_CITE_FORMAT
    var onlyCardYear = false;
    var condense = true;

    companion object {
        const val DEFAULT_CITE_FORMAT = " - (<Author>, <Qualifications><Date> [DOA <CurrentDate>], <Publication>, \"<Title>\", <Url>)"
    }
}