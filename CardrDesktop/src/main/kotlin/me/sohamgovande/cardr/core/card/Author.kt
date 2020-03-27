package me.sohamgovande.cardr.core.card

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import me.sohamgovande.cardr.data.prefs.Prefs

data class Author(val firstName: StringProperty, val lastName: StringProperty, val qualifications: StringProperty = SimpleStringProperty("")) {

    constructor(first: String, last: String): this(SimpleStringProperty(first), SimpleStringProperty(last), SimpleStringProperty(""))

    fun toString(nameFormat: AuthorNameFormat): String {
        val ret: String
        if (nameFormat == AuthorNameFormat.LAST_NAME || firstName.get().isEmpty()) {
            ret = lastName.get()
        } else if (nameFormat == AuthorNameFormat.FIRST_NAME || lastName.get().isEmpty()) {
            ret = firstName.get()
        } else /*if (nameFormat == AuthorNameFormat.FULL_NAME)*/ {
            ret = "${firstName.get()} ${lastName.get()}"
        }
        if (Prefs.get().capitalizeAuthors)
            return ret.toUpperCase()
        return ret
    }

    override fun toString(): String {
        return "Author(firstName='${firstName.get()}', lastName='${lastName.get()}', qualifications='${qualifications.get()}')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Author

        if (firstName.get() != other.firstName.get()) return false
        if (lastName.get() != other.lastName.get()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = firstName.get().hashCode()
        result = 31 * result + lastName.get().hashCode()
        return result
    }


}