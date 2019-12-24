package me.matrix4f.cardcutter.prefs

import com.google.gson.GsonBuilder
import me.matrix4f.cardcutter.CardCutterApplication
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

object Prefs {

    private val path = Paths.get("CardifySettings.json")
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
                var keepSettings = true
                if (readObject.lastUsedVersionInt < CardCutterApplication.CURRENT_VERSION_INT) {
                    // TODO: Show dialog and ask user to confirm their settings will be reset
                }
                if (keepSettings) {
                    prefs = readObject
                } else {
                    save()
                }
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

