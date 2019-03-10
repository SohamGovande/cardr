package me.matrix4f.cardcutter

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.VBox
import javafx.stage.Stage
import me.matrix4f.cardcutter.ui.MainUI
import me.matrix4f.cardcutter.util.recordTime
import me.matrix4f.cardcutter.util.startTime
import me.matrix4f.cardcutter.web.UrlDocReader
import java.util.*

var ui: MainUI? = null

class CardCutterApplication: Application() {

    override fun start(stage: Stage) {

        stage.title = "CardCutter for Debate"
        stage.isResizable = false
        stage.width = WIDTH
        stage.height = HEIGHT
        stage.show()

        ui = MainUI()
        stage.scene = Scene(ui!!.initialize(), WIDTH, HEIGHT)
        ui!!.doDeferredLoad()
        stage.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))
    }

    companion object {
        const val WIDTH = 800.0
        const val HEIGHT = 600.0

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size == 1) {
                Thread {
                    val reader = UrlDocReader(args[0])
                    println(args[0])
                    while (ui?.loaded != true) {
                        // Wait
                    }
                    ui?.loadFromReader(reader)
                }.start()
            }
            startTime()
            launch(CardCutterApplication::class.java)
        }
    }
}