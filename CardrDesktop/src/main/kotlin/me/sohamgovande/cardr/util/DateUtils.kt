package me.sohamgovande.cardr.util

import java.time.LocalDateTime

fun currentDate(): LocalDateTime {
    return LocalDateTime.now()
}

fun convertMonthNameToNumber(word: String): String {
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

fun convertMonthNumberToName(num: String): String {
    return when(num.toLowerCase()) {
       "1" -> "January"
       "2" -> "February"
       "3" -> "March"
       "4" -> "April"
       "5" -> "May"
       "6" -> "June"
       "7" -> "July"
       "8" -> "August"
       "9" -> "September"
       "10" -> "October"
       "11" -> "November"
       "12" -> "December"
        else -> "No month"
    }
}

fun ensureYYYYFormat(yy: String): String {
    if (yy.length == 2) return "20$yy"
    return yy
}