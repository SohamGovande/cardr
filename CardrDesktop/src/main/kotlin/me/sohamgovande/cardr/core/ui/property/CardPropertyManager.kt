package me.sohamgovande.cardr.core.ui.property

import javafx.scene.control.Label
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.core.web.WebsiteCardCutter

class CardPropertyManager(private val cardrUI: CardrUI) {

    var cardProperties = mutableListOf(
        UrlCardProperty(cardrUI),
        DateCardProperty(cardrUI),
        PublicationCardProperty(cardrUI),
        TitleCardProperty(cardrUI),
        CardTagCardProperty(cardrUI),
        AuthorsCardProperty(cardrUI)
    )
    // TODO: Move this to Prefs
    private var activeProperties = mutableListOf(0, 1, 2, 3, 4, 5)

    private lateinit var pGrid: GridPane

    fun generatePropertyGrid(): GridPane {
        pGrid = GridPane()

        pGrid.hgap = 10.0
        pGrid.vgap = 10.0
        pGrid.minWidth = 300.0
        pGrid.prefHeight = CardrDesktop.HEIGHT - 100 // Take up the rest remaining space
        pGrid.columnConstraints.add(ColumnConstraints(60.0))
        pGrid.columnConstraints.add(ColumnConstraints(225.0))

        for ((counter, propertyIndex) in activeProperties.withIndex()) {
            val property = cardProperties[propertyIndex]

            pGrid.add(Label(property.name), 0, counter)
            pGrid.add(property.generateEditUI(), 1, counter)

        }
        return pGrid
    }

    fun <T : CardProperty>getByName(name: String): T? {
        @Suppress("UNCHECKED_CAST")
        for (property in cardProperties)
            if (property.name == name)
                return property as T
        return null
    }

    fun applyMacros(macros: String): String {
        var modify = macros
        for (property in cardProperties) {
            for (macro in property.macros) {
                if (modify.contains(macro)) {
                    modify = modify.replace(macro, property.resolveMacro(macro))
                }
            }
        }
        return modify
    }

    fun bindAllToWebView() {
        for (property in cardProperties)
            property.bindProperties()
    }

    fun loadFromReader(reader: WebsiteCardCutter) {
        for (property in cardProperties) {
            property.loadFromReader(reader)
        }
    }

    fun onWindowResized(stage: Stage) {
        pGrid.prefHeight = stage.height - 150
    }
}