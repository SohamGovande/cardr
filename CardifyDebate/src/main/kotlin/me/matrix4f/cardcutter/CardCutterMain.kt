package me.matrix4f.cardcutter

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import me.matrix4f.cardcutter.ui.CardCuttingUI
import me.matrix4f.cardcutter.web.WebsiteCardCutter
import org.apache.logging.log4j.LogManager

lateinit var ui: CardCuttingUI

class CardifyDebate: Application() {

    override fun start(stage: Stage) {
        logger.info("Launched Cardify")
        stage.title = "CardifyDebate $CURRENT_VERSION"
        stage.isResizable = false
        stage.width = WIDTH
        stage.height = HEIGHT
        logger.info("Initialized window properties")
        stage.show()
        logger.info("Window shown")

        logger.info("Loading window components...")
        try {
            ui = CardCuttingUI(stage)
            stage.scene = Scene(ui.initialize(), WIDTH, HEIGHT)

            logger.info("Loading styles.css")
            stage.scene.stylesheets.add(javaClass.getResource("/styles.css").toExternalForm());
            logger.info("Loading window icon")
            stage.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))

            logger.info("Loading deferred components")
            ui.doDeferredLoad()
            logger.info("... Success")
        } catch (e: Exception) {
            logger.error("Error loading window", e)
        }
    }

    companion object {
        const val WIDTH = 800.0
        const val HEIGHT = 600.0

        const val CURRENT_VERSION = "V1.0.0"
        const val CURRENT_VERSION_INT = 0
        val logger = LogManager.getLogger(CardifyDebate::class.java)
    }
}

fun main(args: Array<String>) {
    if (args.size == 1) {
        Thread {
            val reader = WebsiteCardCutter(args[0])
            println(args[0])
            while (!ui.loaded) { }
            ui.loadFromReader(reader)
        }.start()
    }
    Application.launch(CardifyDebate::class.java)
}