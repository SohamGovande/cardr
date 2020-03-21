package me.matrix4f.cardcutter.ui.windows

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.image.Image
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.WindowEvent
import me.matrix4f.cardcutter.CardifyDebate
import me.matrix4f.cardcutter.prefs.Prefs
import me.matrix4f.cardcutter.updater.CardifyVersion
import me.matrix4f.cardcutter.updater.UpdateExecutor


class UpdateWindow(private val version: CardifyVersion) : ModalWindow("Cardify Updater") {

    private val box = VBox()
    private val subheader = Label("Ready to install Cardify ${version.name}?")
    private val updateBtn = Button("Update Now")
    private val updater = UpdateExecutor(version)
    private val updaterThread = Thread {
        updater.update()
    }

    override fun close(event: WindowEvent?) {
        super.close(event)
        if (updaterThread.isAlive)
            updaterThread.interrupt()
    }

    private fun onClickUpdateBtn() {
        box.children.remove(updateBtn)
        box.children.remove(subheader)

        val status = Label("Installing update...")
        status.isWrapText = true
        val progressBar = ProgressBar()
        progressBar.prefWidth = 225.0

        box.children.add(progressBar)
        box.children.add(status)

        window.sizeToScene()

        updater.messageHandler = {
            Platform.runLater { status.text = it }
        }
        updater.onClose = {
            close(null)
        }
        updaterThread.start()
    }

    override fun generateUI(): Scene {
        CardifyDebate.instance!!.stage.close()
        box.spacing = 5.0
        box.padding = Insets(10.0)

        val header = Label("Update to ${version.name}")
        header.font = Font.font(18.0)

        subheader.font = Font.font(14.0)
        subheader.isWrapText = true

        updateBtn.setOnAction {
            onClickUpdateBtn()
        }

        box.children.add(header)
        box.children.add(subheader)
        box.children.add(updateBtn)

        val scene = Scene(box, WIDTH, HEIGHT)
        scene.stylesheets.add(javaClass.getResource(Prefs.get().getStylesheet()).toExternalForm())
        super.window.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))
        return scene
    }

    companion object {
        const val WIDTH = 250.0
        const val HEIGHT = 150.0
    }
}