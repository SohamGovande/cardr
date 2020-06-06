package me.sohamgovande.cardr.core.ui.property

import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import me.sohamgovande.cardr.core.card.Timestamp
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.core.web.WebsiteCardCutter
import me.sohamgovande.cardr.util.currentDate

class DateCardProperty(cardrUI: CardrUI) : CardProperty("Date", arrayOf("{DateShortened}", "{DateFull}", "{CurrentDate}"), cardrUI) {

    private val dateGrid = GridPane()

    var slashLabelText = "  _  "
    private val slashLabel = Label(slashLabelText)
    private val slashLabel2 = Label(slashLabelText)

    private val propertyDayTF = TextField()
    private val propertyMonthTF = TextField()
    private val propertyYearTF = TextField()
    private var timestamp = Timestamp()

    override fun loadFromReader(reader: WebsiteCardCutter) {
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

    fun loadDateSeparatorLabels() {
        val newText = slashLabelText.replace("_", Timestamp.getSeparator())
        slashLabel.text = newText
        slashLabel2.text = newText
    }

}