package me.sohamgovande.cardr.core.ui.property

import javafx.scene.control.Label
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import me.sohamgovande.cardr.core.ui.tabs.EditCardTabUI
import me.sohamgovande.cardr.core.web.WebsiteCardCutter
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.util.OS
import me.sohamgovande.cardr.util.getOSType

class CardPropertyManager(currentTab: EditCardTabUI) {

    var cardProperties = mutableListOf(
        UrlCardProperty(currentTab),
        DateCardProperty(currentTab),
        PublicationCardProperty(currentTab),
        TitleCardProperty(currentTab),
        CardTagCardProperty(currentTab),
        AuthorsCardProperty(currentTab),
        VolumeCardProperty(currentTab),
        IssueCardProperty(currentTab),
        PagesCardProperty(currentTab)
    )

    private var pGrid = GridPane()

    fun generatePropertyGrid(): GridPane {
        pGrid.children.clear()

        pGrid.hgap = 10.0
        pGrid.vgap = 10.0
        pGrid.minWidth = 300.0
        pGrid.maxWidth = 305.0

        val extraLabelOffset = if (getOSType() == OS.MAC) 10 else 0
        pGrid.columnConstraints.add(ColumnConstraints(60.0 + extraLabelOffset))
        pGrid.columnConstraints.add(ColumnConstraints(225.0 - extraLabelOffset))

        for ((counter, propertyIndex) in Prefs.get().activeProperties.withIndex()) {
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
    }
}
