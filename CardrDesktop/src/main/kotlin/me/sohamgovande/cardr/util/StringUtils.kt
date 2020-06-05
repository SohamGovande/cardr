package me.sohamgovande.cardr.util

import javafx.beans.property.StringProperty
import org.apache.commons.text.StringEscapeUtils

fun applyCorrectCapitalization(input: String): String {
    var nonCapitalChars = 0
    for (c in input) {
        if (c.isLowerCase())
            nonCapitalChars++
    }
    if (nonCapitalChars == 0)
        return input.toLowerCase().capitalize()
    else if (nonCapitalChars == input.length)
        return input.capitalize()
    else
        return input
}

fun unescapeHTML(prop: StringProperty) {
    prop.set(StringEscapeUtils.unescapeCsv(prop.get()))
}