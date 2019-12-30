package me.matrix4f.cardcutter

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import me.matrix4f.cardcutter.scihub.SciHubLoader
import me.matrix4f.cardcutter.ui.CardCuttingUI
import me.matrix4f.cardcutter.util.startTime
import me.matrix4f.cardcutter.web.WebsiteCardCutter
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

lateinit var ui: CardCuttingUI

class CardCutterApplication: Application() {

    override fun start(stage: Stage) {
        stage.title = "CardifyDebate $CURRENT_VERSION"
        stage.isResizable = false
        stage.width = WIDTH
        stage.height = HEIGHT
        stage.show()

        ui = CardCuttingUI()
        stage.scene = Scene(ui.initialize(), WIDTH, HEIGHT)
        stage.scene.stylesheets.add(javaClass.getResource("/CCStyles.css").toExternalForm());
        stage.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))
        ui.doDeferredLoad()
    }

    companion object {
        const val WIDTH = 800.0
        const val HEIGHT = 600.0

        const val CURRENT_VERSION = "V1.0.0"
        const val CURRENT_VERSION_INT = 0

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size == 1) {
                Thread {
                    val reader = WebsiteCardCutter(args[0])
                    println(args[0])
                    while (!ui.loaded) { }
                    ui.loadFromReader(reader)
                }.start()
            }
            startTime()
            launch(CardCutterApplication::class.java)
        }
    }
}