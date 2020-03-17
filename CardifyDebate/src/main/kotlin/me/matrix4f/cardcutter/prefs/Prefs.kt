package me.matrix4f.cardcutter.prefs

import com.google.gson.GsonBuilder
import me.matrix4f.cardcutter.CardifyDebate
import me.matrix4f.cardcutter.prefs.firstlaunch.onFirstLaunch
import me.matrix4f.cardcutter.prefs.firstlaunch.updateFrom
import me.matrix4f.cardcutter.ui.windows.WelcomeWindow
import me.matrix4f.cardcutter.util.OS
import me.matrix4f.cardcutter.util.getOSType
import me.matrix4f.cardcutter.util.showErrorDialog
import me.matrix4f.cardcutter.util.showInfoDialog
import org.apache.logging.log4j.LogManager
import java.awt.Desktop
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object Prefs {

    private val path: Path
    private val gson = GsonBuilder().setPrettyPrinting().setLenient().create()
    private var prefs = PrefsObject()
    private val logger = LogManager.getLogger(Prefs::class.java)

    init {
        if (getOSType() == OS.WINDOWS) {
            path = Paths.get("CardifySettings.json")
        } else {
            path = Paths.get(System.getProperty("user.home"), "CardifyDebate", "CardifySettings.json")
            Files.createDirectories(path.parent)
        }
        read()
    }

    fun get(): PrefsObject = prefs

    fun read() {
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
                            showInfoDialog("Succesfully updated Cardify!", "Updated Cardify from version $lastVersion to ${CardifyDebate.CURRENT_VERSION}.",  "See what's new") {
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
                WelcomeWindow().show()
            } else {
                logger.error("Error occurred while executing first launch tasks", error)
                showErrorDialog(error)
            }
        }
    }
}

