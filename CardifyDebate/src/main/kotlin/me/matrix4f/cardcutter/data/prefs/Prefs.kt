package me.matrix4f.cardcutter.data.prefs

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.matrix4f.cardcutter.CardifyDebate
import me.matrix4f.cardcutter.data.firstlaunch.onFirstLaunch
import me.matrix4f.cardcutter.data.firstlaunch.updateFrom
import me.matrix4f.cardcutter.util.OS
import me.matrix4f.cardcutter.util.getOSType
import me.matrix4f.cardcutter.util.showErrorDialog
import me.matrix4f.cardcutter.util.showInfoDialogUnblocking
import org.apache.logging.log4j.LogManager
import java.awt.Desktop
import java.net.URL
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object Prefs {

    private val path: Path
    private val gson = GsonBuilder().setPrettyPrinting().setLenient().create()
    private var prefs = PrefsObject(null)
    private val logger = LogManager.getLogger(Prefs::class.java)

    init {
        path = Paths.get(System.getProperty("cardifydebate.data.dir"), "CardifySettings.json")
        try { Files.createDirectories(path.parent) } catch (e: FileAlreadyExistsException) { }
        read(true)
    }

    fun get(): PrefsObject = prefs

    fun read(allowCopyOldVersion: Boolean) {
        try {
            if (Files.exists(path)) {
                val readObject: PrefsObject? = gson.fromJson(String(Files.readAllBytes(path)), PrefsObject::class.java)
                // readObject is null if errors were found

                if (readObject == null) {
                    logger.info("Unable to parse preferences")
                    save()
                } else {
                    prefs = readObject
                    logger.info("Read preferences successfully: $prefs")

                    val lastVersion = readObject.lastUsedVersionInt
                    if (lastVersion < CardifyDebate.CURRENT_VERSION_INT) {
                        val error = updateFrom(lastVersion, CardifyDebate.CURRENT_VERSION_INT)
                        if (error == null) {
                            prefs.lastUsedVersionInt = CardifyDebate.CURRENT_VERSION_INT
                            save()
                            showInfoDialogUnblocking("Succesfully updated Cardify!", "Updated Cardify from version $lastVersion to ${CardifyDebate.CURRENT_VERSION}.",  "See what's new") {
                                Desktop.getDesktop().browse(URL("http://cardifydebate.x10.bz/changelog.html").toURI())
                            }
                            logger.info("Successfully updated Cardify from b$lastVersion - saved prefs $prefs")
                        } else {
                            logger.error("Error occurred while updating settings", error)
                            showErrorDialog(error)
                        }
                    } else {
                        save()
                        runFirstLaunch()
                    }
                }
            } else {
                val oldPrefsPath = getOldPrefsPath()
                if (Files.exists(oldPrefsPath) && allowCopyOldVersion) {
                    val oldPrefsData = String(Files.readAllBytes(oldPrefsPath))
                    val oldPrefsJson = JsonParser().parse(oldPrefsData) as JsonObject
                    val version1_1_0 =  2
                    if (oldPrefsJson["lastUsedVersionInt"].asInt <= version1_1_0) {
                        Files.copy(oldPrefsPath, path)
                        read(false)
                        return
                    }
                }

                if (getOSType() == OS.MAC) {
                    prefs.cardFormat = prefs.cardFormat.replace("Calibri", PrefsObject.MAC_CALIBRI_FONT)
                    save()
                }
                runFirstLaunch()
            }
        } catch (e: Exception) {
            logger.error("Unable to read preferences", e)
        }
    }

    private fun getOldPrefsPath(): Path {
        if (getOSType() == OS.MAC)
            return Paths.get(System.getProperty("cardifydebate.data.dir"), "CardifySettings.json")
        else
            return Paths.get(System.getProperty("user.home"), "AppData", "Local", "CardifyDebate", "app", "CardifySettings.json")
    }

    fun save() {
        Files.write(
            path,
            gson.toJson(prefs, PrefsObject::class.java).toByteArray()
        )
    }

    private fun runFirstLaunch() {
        if (prefs.lastFirstLaunchVersion < CardifyDebate.CURRENT_VERSION_INT && prefs.lastFirstLaunchVersion == -1) {
            CardifyDebate.IS_FIRST_LAUNCH = true
            logger.info("Running first launch - found version ${prefs.lastFirstLaunchVersion} but expected ${CardifyDebate.CURRENT_VERSION_INT}/${CardifyDebate.CURRENT_VERSION}")
            val error = onFirstLaunch()
            if (error == null) {
                prefs.lastFirstLaunchVersion = CardifyDebate.CURRENT_VERSION_INT
                save()
                logger.info("Successfully initialized first launch properties - saving prefs as $prefs")
                CardifyDebate.WAS_FIRST_LAUNCH_SUCCESSFUL = true
            } else {
                logger.error("Error occurred while executing first launch tasks", error)
                showErrorDialog(error)
            }
        }
    }
}

