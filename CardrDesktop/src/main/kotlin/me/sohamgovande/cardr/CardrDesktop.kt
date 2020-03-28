package me.sohamgovande.cardr

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.WindowEvent
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.core.ui.WindowDimensions
import me.sohamgovande.cardr.core.ui.windows.ModalWindow
import me.sohamgovande.cardr.core.ui.windows.WelcomeWindow
import org.apache.logging.log4j.LogManager

class CardrDesktop: Application() {

    lateinit var stage: Stage

    private fun onWindowClose(@Suppress("UNUSED_PARAMETER") event: WindowEvent) {
        Prefs.get().windowDimensions = WindowDimensions(stage)
        Prefs.save()
    }

    override fun start(stage: Stage) {
        instance = this
        this.stage = stage
        try {
            logger.info("Launched Cardr")
            stage.title = "cardr ${CURRENT_VERSION}"
            stage.isResizable = true
            stage.width = WIDTH
            stage.height = HEIGHT
            stage.setOnCloseRequest(this::onWindowClose)
            logger.info("Initialized window properties")

            var changedWindowDimensions = false
            val windowDimensions = Prefs.get().windowDimensions
            logger.info("Applying window dimensions: $windowDimensions")
            @Suppress("SENSELESS_COMPARISON")
            if (windowDimensions != null && windowDimensions.x != -1024.1024) {
                changedWindowDimensions = true
                windowDimensions.apply(stage)
            }

            stage.show()
            logger.info("Window shown")

            logger.info("Loading window components...")

            ui = CardrUI(stage)
            stage.scene = Scene(ui!!.initialize())

            stage.scene.stylesheets.add(javaClass.getResource(Prefs.get().getStylesheet()).toExternalForm())
            stage.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))

            logger.info("Loading deferred components")
            ui!!.doDeferredLoad()
            synchronized(uiLock) {
                uiLock.notifyAll()
            }
            logger.info("... Success")

            if (!changedWindowDimensions) {
                Prefs.get().windowDimensions = WindowDimensions(stage)
                Prefs.save()
                stage.sizeToScene()
            } else {
                ui!!.onWindowResized()
            }

            if (IS_FIRST_LAUNCH && WAS_FIRST_LAUNCH_SUCCESSFUL) {
                WelcomeWindow().show()
            }
        } catch (e: Exception) {
            logger.error("Error loading window", e)
        }
    }

    companion object {
        const val WIDTH = 850.0
        const val HEIGHT = 600.0

        const val CURRENT_VERSION = "v1.2.0"
        const val CURRENT_VERSION_INT = 3
        var IS_FIRST_LAUNCH = false
        var WAS_FIRST_LAUNCH_SUCCESSFUL = false
        val OVERRIDE_LOGIN_CHECK = false
        val RELEASE_MODE = true
        val FORCE_AUTOUPDATE = false

        var instance: CardrDesktop? = null

        val logger = LogManager.getLogger(CardrDesktop::class.java)
    }
}