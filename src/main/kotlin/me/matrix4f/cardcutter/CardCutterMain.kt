package me.matrix4f.cardcutter

import javafx.application.Application
import javafx.scene.Scene
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
        recordTime("main")
        stage.title = "CardCutter for Debate"
        stage.resizableProperty().set(false)
        stage.scene = Scene(VBox(), WIDTH, HEIGHT)
        stage.sizeToScene()
        stage.show()

        ui = MainUI()
        stage.scene = Scene(ui!!.initialize(), WIDTH, HEIGHT)
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