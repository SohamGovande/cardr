package me.sohamgovande.cardr.core.ui.property

import com.google.gson.JsonObject
import javafx.scene.Node
import javafx.scene.control.TextField
import me.sohamgovande.cardr.core.ui.tabs.EditCardTabUI
import me.sohamgovande.cardr.core.web.CardWebScraper

abstract class CardProperty(val name: String, val macros: Array<String>, val currentTab: EditCardTabUI) {

    abstract fun loadFromReader(reader: CardWebScraper)
    abstract fun resolveMacro(macro: String): String
    abstract fun generateEditUI(): Node

    abstract fun bindProperties()
    abstract fun loadFromJson(data: JsonObject)
    abstract fun saveToJson(): JsonObject

    protected fun bindToRefreshWebView(component: TextField) {
        component.textProperty().addListener(currentTab.changeListenerUpdateHTML)
    }
}