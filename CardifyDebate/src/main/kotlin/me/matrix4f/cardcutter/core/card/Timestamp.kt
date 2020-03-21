package me.matrix4f.cardcutter.core.card

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import me.matrix4f.cardcutter.data.prefs.Prefs
import me.matrix4f.cardcutter.util.convertMonthNumberToName
import me.matrix4f.cardcutter.util.currentDate

class Timestamp {

    var year: StringProperty = SimpleStringProperty("")
    var month: StringProperty = SimpleStringProperty("")
    var day: StringProperty = SimpleStringProperty("")

    fun yearAsInt(): Int? {
        return if (hasYear()) year.get().toInt() else null
    }

    private fun hasDay(): Boolean = !day.get().isNullOrEmpty()
    private fun hasYear(): Boolean = !year.get().isNullOrEmpty()
    private fun hasMonth(): Boolean = !month.get().isNullOrEmpty()

    fun toString(fullDate: Boolean): String {
        val yearIntOrNull = yearAsInt()
        if (yearIntOrNull == null) {
            return "No Date"
        } else {
            if (fullDate) {
                // Either we don't know the month or neither the month and the day
                if (!hasMonth() || (!hasMonth() && !hasDay()))
                    return year.get().toString()

                // We know the month and year but not the day (e.g. Jan 19)
                else if (!hasDay()) {
                    if (month.get().length <= 2) {
                        // Month is a number
                        return "${convertMonthNumberToName(month.get())} ${year.get()}"
                    } else {
                        // Month is a string (jan/feb/mar)
                        return "${month.get().substring(0, 3)} ${year.get()}"
                    }
                }

                else
                    return "${month.get()}-${day.get()}-${year.get()}"
            } else {
                val yearInt = yearIntOrNull.toInt()
                val date = currentDate()
                if (yearInt == date.year && !Prefs.get().onlyCardYear) {
                    // Card is from this year
                    return "${month.get()}-${day.get()}"
                } else {
                    // Card is from a previous year

                    if (yearInt >= 2000)
                        // 2019 becomes 19, 2005 becomes 5
                        return (yearInt - 2000).toString()
                    else
                        // 1985 stays 1985
                        return yearInt.toString()
                }
            }
        }
    }

    override fun toString(): String {
        return "Timestamp(year=${year.get()}, month=${month.get()}, day=${day.get()})"
    }


}