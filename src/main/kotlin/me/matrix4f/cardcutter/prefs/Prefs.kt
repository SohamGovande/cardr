package me.matrix4f.cardcutter.prefs

import com.google.gson.GsonBuilder
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

object Prefs {

    private val path = Paths.get("CCSettings.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private var prefs = PrefsObject()

    init {
        read()
    }

    fun get(): PrefsObject = prefs

    fun read() {
        if (Files.exists(path)) {
            prefs = gson.fromJson(String(Files.readAllBytes(path)), PrefsObject::class.java)
        }
    }

    fun save() {
        Files.write(path, gson.toJson(this, javaClass).toByteArray(), StandardOpenOption.WRITE, StandardOpenOption.CREATE)
    }
}

