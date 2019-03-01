package me.matrix4f.cardcutter.card

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

data class Author(val firstName: StringProperty, val lastName: StringProperty, val qualifications: StringProperty = SimpleStringProperty("")) {

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
}