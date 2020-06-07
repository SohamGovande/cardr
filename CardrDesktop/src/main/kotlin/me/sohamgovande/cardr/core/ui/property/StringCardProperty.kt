package me.sohamgovande.cardr.core.ui.property

import javafx.beans.property.SimpleStringProperty
import javafx.scene.Node
import javafx.scene.control.TextField
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.core.web.WebsiteCardCutter

abstract class StringCardProperty(name: String, macro: String, cardrUI: CardrUI) : CardProperty(name, arrayOf("{$macro}"), cardrUI) {

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

class PublicationCardProperty(cardrUI: CardrUI) : StringCardProperty("Publication", "Publication", cardrUI) {
    override fun loadFromReader(reader: WebsiteCardCutter) {
        setValue(reader.getPublication())
    }
}

class TitleCardProperty(cardrUI: CardrUI) : StringCardProperty("Title", "Title", cardrUI) {

    init {
        getValueProperty().addListener { _, _, value ->
            cardrUI.updateWindowTitle(value)
        }
    }
    override fun loadFromReader(reader: WebsiteCardCutter) {
        setValue(reader.getTitle() ?: "")
    }
}

class UrlCardProperty(cardrUI: CardrUI) : StringCardProperty("URL", "Url", cardrUI) {
    override fun loadFromReader(reader: WebsiteCardCutter) {
        setValue(reader.getURL())
    }
}

class CardTagCardProperty(cardrUI: CardrUI) : StringCardProperty("Card Tag", "Tag", cardrUI) {
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

class VolumeCardProperty(cardrUI: CardrUI) : StringCardProperty("Volume", "Volume", cardrUI) {
    override fun loadFromReader(reader: WebsiteCardCutter) {}
}

class IssueCardProperty(cardrUI: CardrUI) : StringCardProperty("Issue", "Issue", cardrUI) {
    override fun loadFromReader(reader: WebsiteCardCutter) {}
}

class PagesCardProperty(cardrUI: CardrUI) : StringCardProperty("Pages", "Pages", cardrUI) {
    override fun loadFromReader(reader: WebsiteCardCutter) {}
}