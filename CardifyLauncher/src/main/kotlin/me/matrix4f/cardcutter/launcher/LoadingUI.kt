package me.matrix4f.cardcutter.launcher

import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import org.apache.logging.log4j.LogManager

class LoadingUI {

    fun initialize(): Scene {
        val box = VBox()

        box.spacing = 5.0
        box.padding = Insets(10.0)

        val header = Label("Launching Cardify...")
        header.font = Font.font(18.0)

        val subheader = Label("Please wait.")
        subheader.font = Font.font(14.0)

        box.children.add(header)
        box.children.add(subheader)

        return Scene(box, UpdaterUI.WIDTH, UpdaterUI.HEIGHT)
    }

    companion object {
        const val WIDTH = 250.0
        const val HEIGHT = 100.0

        val logger = LogManager.getLogger(UpdaterUI::class.java)
    }
}