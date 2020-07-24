package me.sohamgovande.cardr.core.ui.property

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Node
import javafx.scene.control.TextField
import me.sohamgovande.cardr.core.ui.tabs.EditCardTabUI
import me.sohamgovande.cardr.core.web.CardWebScraper

abstract class StringCardProperty(name: String, macro: String, currentTab: EditCardTabUI) : CardProperty(name, arrayOf("{$macro}"), currentTab) {

    private val value = SimpleStringProperty("")
    private val textField = TextField()

    init {
        textField.textProperty().bindBidirectional(value)
    }

    abstract override fun loadFromReader(reader: CardWebScraper)

    override fun loadFromJson(data: JsonObject) {
        value.set(data["value"].asString)
    }

    override fun saveToJson(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.add("value", JsonPrimitive(value.get()))
        return jsonObject
    }

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
    override fun loadFromReader(reader: CardWebScraper) {
        setValue(reader.getPublication())
    }
}

class TitleCardProperty(currentTab: EditCardTabUI) : StringCardProperty("Title", "Title", currentTab) {

    init {
        getValueProperty().addListener { _, _, value ->
            currentTab.updateWindowTitle(value)
        }
    }
    override fun loadFromReader(reader: CardWebScraper) {
        setValue(reader.getTitle() ?: "")
    }
}

class UrlCardProperty(currentTab: EditCardTabUI) : StringCardProperty("URL", "Url", currentTab) {
    override fun loadFromReader(reader: CardWebScraper) {
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

    override fun loadFromReader(reader: CardWebScraper) {
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
    override fun loadFromReader(reader: CardWebScraper) {}
}

class IssueCardProperty(currentTab: EditCardTabUI) : StringCardProperty("Issue", "Issue", currentTab) {
    override fun loadFromReader(reader: CardWebScraper) {}
}

class PagesCardProperty(currentTab: EditCardTabUI) : StringCardProperty("Pages", "Pages", currentTab) {
    override fun loadFromReader(reader: CardWebScraper) {}
}