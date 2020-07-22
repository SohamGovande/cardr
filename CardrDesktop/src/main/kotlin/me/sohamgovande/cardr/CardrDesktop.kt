package me.sohamgovande.cardr

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.stage.WindowEvent
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.core.ui.WindowDimensions
import me.sohamgovande.cardr.core.ui.windows.WelcomeWindow
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.util.OS
import me.sohamgovande.cardr.util.getOSType
import org.apache.logging.log4j.LogManager
import kotlin.system.exitProcess

class CardrDesktop: Application() {

    lateinit var stage: Stage

    private fun onWindowClose(@Suppress("UNUSED_PARAMETER") event: WindowEvent) {
        Prefs.get().windowDimensions = WindowDimensions(stage)
        Prefs.save()
        logger.info("Window close event received. Exiting normally.")
        exitProcess(0)
    }

    override fun start(stage: Stage) {
        instance = this
        this.stage = stage
        try {
            logger.info("Launched Cardr")
            stage.title = "cardr $CURRENT_VERSION"
            stage.isResizable = true
            stage.width = WIDTH
            stage.height = HEIGHT
            stage.setOnCloseRequest(this::onWindowClose)
            logger.info("Initialized window properties")

            var changedWindowDimensions = false
            val windowDimensions = Prefs.get().windowDimensions
            logger.info("Applying window dimensions: $windowDimensions")
            if (windowDimensions.x != -1024.1024) {
                changedWindowDimensions = true
                windowDimensions.apply(stage)
            }

            stage.scene = Scene(VBox()) // Create a dummy scene - fixes bug when displaying w/ dual monitors
            stage.show()
            logger.info("Window shown")

            logger.info("Loading window components...")

            ui = CardrUI(stage)
            synchronized(constructorUiLock) {
                logger.info("Notifying lock: 'finishedConstructorUiLock'")
                constructorUiLock.notifyAll()
            }
            stage.scene = Scene(ui!!.initialize())

            stage.scene.stylesheets.add(javaClass.getResource(Prefs.get().getStylesheet()).toExternalForm())
            stage.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))

            logger.info("Loading deferred components")
            ui!!.doDeferredLoad()
            synchronized(finishedLoadingUiLock) {
                logger.info("Notifying lock: 'finishedLoadingUiLock'")
                finishedLoadingUiLock.notifyAll()
            }
            logger.info("... Success")

            if (!changedWindowDimensions) {
                Prefs.get().windowDimensions = WindowDimensions(stage)
                Prefs.save()
                stage.sizeToScene()
            }

            if (IS_FIRST_LAUNCH && WAS_FIRST_LAUNCH_SUCCESSFUL) {
                WelcomeWindow(ui!!).show()
            }

            if (getOSType() == OS.MAC) {
                logger.info("Generating mac menu bar")
                ui!!.menubarHelper.applyMacMenu()
            }

            ui!!.onWindowResized()
        } catch (e: Throwable) {
            logger.error("Error loading window", e)
        }
    }

    companion object {
        const val WIDTH = 835.0
        const val HEIGHT = 600.0

        const val CURRENT_VERSION = "v1.5.0"
        const val CURRENT_VERSION_INT = 7
        var IS_FIRST_LAUNCH = false
        var WAS_FIRST_LAUNCH_SUCCESSFUL = false

        const val OVERRIDE_LOGIN_CHECK = false
        const val RELEASE_MODE = true
        const val FORCE_AUTO_UPDATE = false

        var instance: CardrDesktop? = null

        val logger = LogManager.getLogger(CardrDesktop::class.java)
    }
}
