package me.sohamgovande.cardr.data.urls

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.LogManager
import java.awt.Desktop
import java.net.URL

object UrlHelper {

    private val logger = LogManager.getLogger(UrlHelper::class.java)

    private lateinit var data: JsonObject

    init {
        read()
    }

    fun read() {
        try {
            val stream = javaClass.getResourceAsStream("/urls.json")
            val jsonStr = IOUtils.toString(stream)
            IOUtils.closeQuietly(stream)
            data = JsonParser().parse(jsonStr) as JsonObject
        } catch (e: Exception) {
            logger.error("Unable to read url data", e)
        }
    }

    fun get(key: String): String {
        return data[key].asString
    }

    fun url(key: String): URL {
        return URL(get(key))
    }

    fun browse(key: String) {
        Desktop.getDesktop().browse(URL(get(key)).toURI())
    }
}