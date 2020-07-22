package me.sohamgovande.cardr.core.ui.property

import javafx.scene.Node
import javafx.scene.control.TextField
import me.sohamgovande.cardr.core.ui.tabs.EditCardTabUI
import me.sohamgovande.cardr.core.web.WebsiteCardCutter

abstract class CardProperty(val name: String, val macros: Array<String>, val currentTab: EditCardTabUI) {

    abstract fun loadFromReader(reader: WebsiteCardCutter)
    abstract fun resolveMacro(macro: String): String
    abstract fun generateEditUI(): Node

    abstract fun bindProperties()

    protected fun bindToRefreshWebView(component: TextField) {
        component.textProperty().addListener(currentTab.changeListenerUpdateHTML)
    }
}