package me.matrix4f.cardcutter.card

import javafx.beans.property.StringProperty
import me.matrix4f.cardcutter.data.prefs.Prefs

data class Cite(val authors: Array<Author>,
                val date: Timestamp,
                val title: String,
                val publication: String,
                val url: String) {

    fun getAuthorName(nameFormat: AuthorNameFormat): String {
        if (authors.size > 1) {
            if (Prefs.get().useEtAl && nameFormat == AuthorNameFormat.LAST_NAME) {
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cite

        if (!authors.contentEquals(other.authors)) return false
        if (date != other.date) return false
        if (title != other.title) return false
        if (publication != other.publication) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = authors.contentHashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + publication.hashCode()
        result = 31 * result + url.hashCode()
        return result
    }
}