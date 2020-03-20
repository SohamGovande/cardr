package me.matrix4f.cardcutter

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.WindowEvent
import me.matrix4f.cardcutter.prefs.Prefs
import me.matrix4f.cardcutter.ui.CardCuttingUI
import me.matrix4f.cardcutter.ui.WindowDimensions
import me.matrix4f.cardcutter.ui.windows.WelcomeWindow
import org.apache.logging.log4j.LogManager

class CardifyDebate: Application() {

    lateinit var stage: Stage

    private fun onWindowClose(event: WindowEvent) {
        Prefs.get().windowDimensions = WindowDimensions(stage)
        Prefs.save()
    }

    override fun start(stage: Stage) {
        instance = this
        this.stage = stage
        try {
            logger.info("Launched Cardify")
            stage.title = "Cardify Debate"
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

            stage.show()
            logger.info("Window shown")

            logger.info("Loading window components...")

            ui = CardCuttingUI(stage)
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
        const val WIDTH = 815.0
        const val HEIGHT = 600.0

        const val CURRENT_VERSION = "v1.2.0"
        const val CURRENT_VERSION_INT = 3
        var IS_FIRST_LAUNCH = false
        var WAS_FIRST_LAUNCH_SUCCESSFUL = false
        val OVERRIDE_LOGIN_CHECK = true
        val RELEASE_MODE = true

        var instance: CardifyDebate? = null

        val logger = LogManager.getLogger(CardifyDebate::class.java)
    }
}