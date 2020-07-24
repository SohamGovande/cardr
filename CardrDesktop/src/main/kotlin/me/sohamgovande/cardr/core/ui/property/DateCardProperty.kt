package me.sohamgovande.cardr.core.ui.property

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import me.sohamgovande.cardr.core.card.Timestamp
import me.sohamgovande.cardr.core.ui.tabs.EditCardTabUI
import me.sohamgovande.cardr.core.web.CardWebScraper
import me.sohamgovande.cardr.util.currentDate

class DateCardProperty(currentTab: EditCardTabUI) : CardProperty("Date", arrayOf("{DateShortened}", "{DateFull}", "{CurrentDate}"), currentTab) {

    private val dateGrid = GridPane()

    var slashLabelText = "  _  "
    private val slashLabel = Label(slashLabelText)
    private val slashLabel2 = Label(slashLabelText)

    private val propertyDayTF = TextField()
    private val propertyMonthTF = TextField()
    private val propertyYearTF = TextField()
    private var timestamp = Timestamp()

    override fun loadFromReader(reader: CardWebScraper) {
        timestamp = reader.getDate()

        propertyDayTF.textProperty().bindBidirectional(this.timestamp.day)
        propertyMonthTF.textProperty().bindBidirectional(this.timestamp.month)
        propertyYearTF.textProperty().bindBidirectional(this.timestamp.year)
    }

    override fun resolveMacro(macro: String): String {
        return when (macro) {
            "{DateFull}" -> timestamp.toString(true)
            "{DateShortened}" -> timestamp.toString(false)
            "{CurrentDate}" -> {
                val now = currentDate()
                "${now.monthValue}${Timestamp.getSeparator()}${now.dayOfMonth}${Timestamp.getSeparator()}${now.year}"
            }
            else -> ""
        }
    }

    override fun generateEditUI(): Node {
        dateGrid.children.clear()

        propertyDayTF.prefColumnCount = 2
        propertyDayTF.promptText = "31"

        propertyMonthTF.prefColumnCount = 2
        propertyMonthTF.promptText = "01"

        propertyYearTF.prefColumnCount = 4
        propertyYearTF.promptText = currentDate().year.toString()

        dateGrid.padding = Insets(0.0)
        dateGrid.add(propertyMonthTF, 0, 0)
        dateGrid.add(slashLabel, 1, 0)
        dateGrid.add(propertyDayTF, 2, 0)
        dateGrid.add(slashLabel2, 3, 0)
        dateGrid.add(propertyYearTF, 4, 0)

        return dateGrid
    }

    override fun bindProperties() {
        bindToRefreshWebView(propertyDayTF)
        bindToRefreshWebView(propertyMonthTF)
        bindToRefreshWebView(propertyYearTF)
    }

    override fun loadFromJson(data: JsonObject) {
        timestamp.day.set(data.get("day").asString)
        timestamp.month.set(data.get("month").asString)
        timestamp.year.set(data.get("year").asString)
    }

    override fun saveToJson(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.add("day", JsonPrimitive(timestamp.day.get()))
        jsonObject.add("month", JsonPrimitive(timestamp.month.get()))
        jsonObject.add("year", JsonPrimitive(timestamp.year.get()))
        return jsonObject
    }

    fun loadDateSeparatorLabels() {
        val newText = slashLabelText.replace("_", Timestamp.getSeparator())
        slashLabel.text = newText
        slashLabel2.text = newText
    }

}