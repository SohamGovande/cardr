package me.sohamgovande.cardr.core.card

import javafx.beans.property.StringProperty
import me.sohamgovande.cardr.data.prefs.Prefs

class AuthorListManager(private val authors: Array<Author>) {

    fun getAuthorName(nameFormat: AuthorNameFormat): String {
        if (authors.size > 1) {
            if (Prefs.get().useEtAl && nameFormat == AuthorNameFormat.LAST_NAME && authors.size > 2) {
                return "${authors[0].toString(nameFormat)} et al."
            }

            // Multiple authors - create a list (e.g. "Brooks, Wolfsworth, and Ikenberry")

            val builder = StringBuilder(authors[0].toString(nameFormat))
            for (i in 1 until (authors.size - 1))
                builder.append(", ").append(authors[i].toString(nameFormat))
            if (authors.size > 2)
                builder.append(',')
            builder.append(" and ").append(authors.last().toString(nameFormat))

            return builder.toString()
        } else if (authors.size == 1) {
            return authors[0].toString(nameFormat)
        } else {
            return "No Author"
        }
    }

    fun getAuthorQualifications(): String {
        var ret = authors.map(Author::qualifications)
            .map(StringProperty::get)
            .filter(String::isNotBlank)
            .joinToString { it }

        if (Prefs.get().endQualsWithComma && ret != "") {
           ret += ", "
        }
        return ret
    }
}