package me.matrix4f.cardcutter.installer

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.apache.commons.exec.OS

class CardifyInstaller : Application() {

    private fun checkForWindows() {
        if (!OS.isFamilyWindows()) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.headerText = "This installer is for Windows only!"
            alert.contentText = "Please visit http://cardifydebate.x10.bz/get-started.html to select the installer for your operating system."
            alert.showAndWait()
            System.exit(0)
        }

    }

    override fun start(stage: Stage) {
        checkForWindows()

        stage.title = "CardifyDebate Installer for Windows"
        stage.isResizable = false
        stage.width = WIDTH
        stage.height = HEIGHT
        stage.show()

        val ui = InstallerUI(stage)
        stage.scene = Scene(ui.initialize(), WIDTH, HEIGHT)
        stage.scene.stylesheets.add(javaClass.getResource("/CCStyles.css").toExternalForm());
        stage.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))
    }

    companion object {
        const val WIDTH = 400.0
        const val HEIGHT = 300.0

    }
}

fun main(args: Array<String>) {
    Application.launch(CardifyInstaller::class.java)
}
