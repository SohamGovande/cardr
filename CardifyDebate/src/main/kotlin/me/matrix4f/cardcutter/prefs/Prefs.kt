package me.matrix4f.cardcutter.prefs

import com.google.gson.GsonBuilder
import me.matrix4f.cardcutter.CardCutterApplication
import me.matrix4f.cardcutter.firstlaunch.onFirstLaunch
import me.matrix4f.cardcutter.firstlaunch.showFirstLaunchError
import java.nio.file.Files
import java.nio.file.Paths

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
                if (readObject.lastUsedVersionInt < CardCutterApplication.CURRENT_VERSION_INT) { }
                if (keepSettings) {
                    prefs = readObject
                } else {
                    save()
                }

                if (prefs.lastFirstLaunchVersion < CardCutterApplication.CURRENT_VERSION_INT) {
                    val error = onFirstLaunch()
                    if (error == null) {
                        prefs.lastFirstLaunchVersion = CardCutterApplication.CURRENT_VERSION_INT
                        save()
                    } else {
                        showFirstLaunchError(error)
                    }
                }
            }
        } else {
            val error = onFirstLaunch()
            if (error == null) {
                prefs.lastFirstLaunchVersion = CardCutterApplication.CURRENT_VERSION_INT
                save()
            } else {
                showFirstLaunchError(error)
            }
        }
    }

    fun save() {
        Files.write(
            path,
            gson.toJson(prefs, PrefsObject::class.java).toByteArray()
        )
    }
}

