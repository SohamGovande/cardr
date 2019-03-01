package me.matrix4f.cardcutter.util

import javafx.beans.property.SimpleStringProperty
import me.matrix4f.cardcutter.card.Timestamp
import org.jsoup.nodes.Document
import java.lang.Exception
import java.time.LocalDateTime

fun currentDate() : LocalDateTime {
    return LocalDateTime.now()
}

fun matchRegexDates(doc: Document,
                            regex: String,
                            extractMonth: (groups: MatchGroupCollection) -> String,
                            extractDate: (groups: MatchGroupCollection) -> String,
                            extractYear: (groups: MatchGroupCollection) -> String) : Timestamp? {
    val elements = doc.select("*:matchesOwn($regex)")
    if (elements.size == 0) return null

    for (element in elements) {
        val matchResult = Regex(regex).find(element.text()) ?: continue

        try {
            val ts = Timestamp()
            ts.day = SimpleStringProperty(extractDate(matchResult.groups))
            ts.month = SimpleStringProperty(extractMonth(matchResult.groups))
            ts.year = SimpleStringProperty(extractYear(matchResult.groups))
            return ts
        } catch (e: Exception) {
            e.printStackTrace()
            continue
        }
    }

    return null
}

fun convertMonthNameToNumber(word: String) : String {
    return when(word.toLowerCase()) {
        "jan", "january" -> "1"
        "feb", "february" -> "2"
        "mar", "march" -> "3"
        "apr", "april" -> "4"
        "may" -> "5"
        "jun", "june" -> "6"
        "jul", "july" -> "7"
        "aug", "august" -> "8"
        "sep", "sept", "september" -> "9"
        "oct", "october" -> "10"
        "nov", "november" -> "11"
        "dec", "december" -> "12"
        else -> ""
    }
}

fun ensureYYYYFormat(yy: String): String {
    if (yy.length == 2) return "20$yy"
    return yy
}