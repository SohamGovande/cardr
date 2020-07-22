package me.sohamgovande.cardr.core.ui.property

import javafx.beans.property.SimpleStringProperty
import javafx.scene.Node
import javafx.scene.control.TextField
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.core.ui.tabs.EditCardTabUI
import me.sohamgovande.cardr.core.web.WebsiteCardCutter

abstract class StringCardProperty(name: String, macro: String, currentTab: EditCardTabUI) : CardProperty(name, arrayOf("{$macro}"), currentTab) {

    private val value = SimpleStringProperty("")
    private val textField = TextField()

    init {
        textField.textProperty().bindBidirectional(value)
    }

    abstract override fun loadFromReader(reader: WebsiteCardCutter)

    override fun resolveMacro(macro: String): String {
        if (macro == macros[0])
            return textField.text
        return ""
    }

    override fun generateEditUI(): Node {
        textField.promptText = name
        return textField
    }

    override fun bindProperties() {
        bindToRefreshWebView(textField)
    }

    fun getValueProperty(): SimpleStringProperty {
        return value
    }

    fun getValue(): String {
        return value.get()
    }

    fun setValue(newVal: String) {
        value.set(newVal)
    }
}

class PublicationCardProperty(currentTab: EditCardTabUI) : StringCardProperty("Publication", "Publication", currentTab) {
    override fun loadFromReader(reader: WebsiteCardCutter) {
        setValue(reader.getPublication())
    }
}

class TitleCardProperty(currentTab: EditCardTabUI) : StringCardProperty("Title", "Title", currentTab) {

    init {
        getValueProperty().addListener { _, _, value ->
            currentTab.updateWindowTitle(value)
        }
    }
    override fun loadFromReader(reader: WebsiteCardCutter) {
        setValue(reader.getTitle() ?: "")
    }
}

class UrlCardProperty(currentTab: EditCardTabUI) : StringCardProperty("URL", "Url", currentTab) {
    override fun loadFromReader(reader: WebsiteCardCutter) {
        setValue(reader.getURL())
    }
}

class CardTagCardProperty(currentTab: EditCardTabUI) : StringCardProperty("Card Tag", "Tag", currentTab) {
    init {
        getValueProperty().addListener { _, _, value ->
            for (dash in DASHES) {
                if (value.contains(dash.key))
                    setValue(value.replace(dash.key, dash.value))
            }
        }
    }

    override fun loadFromReader(reader: WebsiteCardCutter) {
        setValue(reader.getTitle() ?: "")
    }

    companion object {
        val DASHES = mapOf(
            Pair("--", "–"),
            Pair("–-","—"),
            Pair("---", "—")
        )
    }
}

class VolumeCardProperty(currentTab: EditCardTabUI) : StringCardProperty("Volume", "Volume", currentTab) {
    override fun loadFromReader(reader: WebsiteCardCutter) {}
}

class IssueCardProperty(currentTab: EditCardTabUI) : StringCardProperty("Issue", "Issue", currentTab) {
    override fun loadFromReader(reader: WebsiteCardCutter) {}
}

class PagesCardProperty(currentTab: EditCardTabUI) : StringCardProperty("Pages", "Pages", currentTab) {
    override fun loadFromReader(reader: WebsiteCardCutter) {}
}