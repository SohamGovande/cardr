package me.matrix4f.cardcutter.util

import javafx.beans.property.SimpleStringProperty
import me.matrix4f.cardcutter.card.Timestamp
import org.jsoup.nodes.Document
import java.lang.Exception

data class RegexDatePattern(val regex: String,
                            val extractMonth: (groups: MatchGroupCollection) -> String,
                            val extractDate: (groups: MatchGroupCollection) -> String,
                            val extractYear: (groups: MatchGroupCollection) -> String)

class DateRegexMatcher {
    val patterns: MutableList<RegexDatePattern> = mutableListOf()

    fun register(pattern: RegexDatePattern) = patterns.add(pattern)

    fun matchRegexDates(doc: Document) : Timestamp? {
        for (pattern in patterns) {
            val elements = doc.select("*:matchesOwn(${pattern.regex})")
            if (elements.size == 0) continue

            for (element in elements) {
                val matchResult = Regex(pattern.regex).find(element.text()) ?: continue

                try {
                    val ts = Timestamp()
                    ts.day = SimpleStringProperty(pattern.extractDate(matchResult.groups))
                    ts.month = SimpleStringProperty(pattern.extractMonth(matchResult.groups))
                    ts.year = SimpleStringProperty(pattern.extractYear(matchResult.groups))
                    return ts
                } catch (e: Exception) {
                    continue
                }
            }
        }

        return null
    }
}