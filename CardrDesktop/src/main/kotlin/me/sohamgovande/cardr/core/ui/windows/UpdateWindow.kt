package me.sohamgovande.cardr.core.ui.windows

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
import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.data.updater.UpdateExecutor
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.data.updater.CardrVersion


class UpdateWindow(private val version: CardrVersion) : ModalWindow("Updater") {

    private val box = VBox()
    private val header = Label("cardr updater")
    private val subheader = Label("Ready to install cardr ${version.name}?")
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
        header.text = "Updating to ${version.name}..."
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
        CardrDesktop.instance!!.stage.close()
        val openWindows = openWindows
        val iter = ModalWindow.openWindows.iterator()
        while (iter.hasNext()) {
            val it = iter.next()
            it.forcedClose = true
            it.autoRemove = false
            it.close(null)
            iter.remove()
        }

        box.spacing = 5.0
        box.padding = Insets(10.0)

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
        const val HEIGHT = 115.0
    }
} 
