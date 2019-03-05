package me.matrix4f.cardcutter.card

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

data class Author(val firstName: StringProperty, val lastName: StringProperty, val qualifications: StringProperty = SimpleStringProperty("")) {

    constructor(first: String, last: String) : this(SimpleStringProperty(first), SimpleStringProperty(last), SimpleStringProperty(""))

    fun toString(short: Boolean): String {
        return if (short) {
            lastName.get()
        } else {
            ("${firstName.get()} ${lastName.get()}")
        }
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