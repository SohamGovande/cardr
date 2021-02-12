package me.sohamgovande.cardr.data.files

import com.google.gson.JsonObject
import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.core.ui.property.CardProperty
import me.sohamgovande.cardr.core.ui.property.UrlCardProperty
import me.sohamgovande.cardr.core.ui.tabs.EditCardTabUI
import me.sohamgovande.cardr.util.CardrException
import me.sohamgovande.cardr.util.getTodayAsString
import org.apache.logging.log4j.LogManager
import java.util.*

data class FSCardData(
    var uuid: UUID,
    var filename: String? = null,
    var lastCardrVersion: Int,
    var modifiedDate: String,

    var tags: MutableList<String>,
    var removeWords: List<String> = listOf(),
    var removeParagraphs: List<String> = listOf(),

    var cardBody: String = "",
    var overrideBodyParagraphs: MutableList<String>? = mutableListOf(),
    var overrideBodyHTML: String? = null,

    @Transient var cardProperties: MutableList<CardProperty> = mutableListOf(),
    @Transient var cardPropertiesJson: JsonObject = JsonObject()
) : FSObj() {

    constructor(tab: EditCardTabUI) : this(UUID.randomUUID(), null, 0, "", mutableListOf()) {
        uuid = UUID.randomUUID()
        lastCardrVersion = CardrDesktop.CURRENT_VERSION_INT
        modifiedDate = getTodayAsString()
        cardProperties = tab.propertyManager.cardProperties
        removeWords = tab.removeWords
        removeParagraphs = tab.removeParagraphs

        cardBody = tab.cardBody.get()
        overrideBodyHTML = tab.overrideBodyHTML
        overrideBodyParagraphs = tab.overrideBodyParagraphs
    }

    fun createNewTab(cardrUI: CardrUI) {
        val tab = cardrUI.createNewEditTab(null)
        loadProperties(tab)
        tab.urlTF.text = tab.propertyManager.getByName<UrlCardProperty>("URL")!!.getValue()
        tab.refreshHTML()
    }

    fun setProperties(propertiesObj: JsonObject) {
        this.cardPropertiesJson = propertiesObj
    }

    fun loadProperties(tab: EditCardTabUI) {
        for (propertyJson in cardPropertiesJson.entrySet()) {
            val pName = propertyJson.key
            val property = tab.propertyManager.getByName<CardProperty>(pName)
            if (property != null) {
                property.loadFromJson(propertyJson.value.asJsonObject)
            } else {
                logger.error("Unable to set property $pName - not found")
            }
        }
    }

    fun <T : CardProperty>findDummyProperty(dummy: EditCardTabUI, name: String): T? {
        for (propertyJson in cardPropertiesJson.entrySet()) {
            if (propertyJson.key == name) {
                @Suppress("UNCHECKED_CAST")
                val prop = dummy.propertyManager.cardProperties.first { it.name == name } as T
                prop.loadFromJson(propertyJson.value.asJsonObject)
                return prop
            }
        }
        return null
    }

    fun getPropertiesJson(): JsonObject {
        if (cardProperties.isEmpty())
            throw CardrException("Card properties are empty - this should never happen")
        val jsonObject = JsonObject()
        for (property in cardProperties)
            jsonObject.add(property.name, property.saveToJson())
        return jsonObject
    }

    companion object {
        val logger = LogManager.getLogger(FSCardData::class.java)
    }
}