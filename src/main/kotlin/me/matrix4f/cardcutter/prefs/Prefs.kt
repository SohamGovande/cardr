package me.matrix4f.cardcutter.prefs

import com.google.gson.GsonBuilder
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

object Prefs {

    public val path = Paths.get("CCSettings.json")
    private val gson = GsonBuilder().setPrettyPrinting().setLenient().create()
    private var prefs = PrefsObject()

    init {
        read()
    }

    fun get(): PrefsObject = prefs

    fun read() {
        if (Files.exists(path)) {
            val readObject: PrefsObject? = gson.fromJson(String(Files.readAllBytes(path)), PrefsObject::class.java)
            // readObject is null if errors were found
            if (readObject == null) {
                save()
            } else {
                prefs = readObject
            }
        } else {
            save()
        }
    }

    fun save() {
        Files.write(
            path,
            gson.toJson(prefs, PrefsObject::class.java).toByteArray()
        )
    }
}

