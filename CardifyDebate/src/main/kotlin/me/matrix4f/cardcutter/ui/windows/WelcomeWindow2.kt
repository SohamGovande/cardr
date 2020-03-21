package me.matrix4f.cardcutter.ui.windows

import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.WindowEvent
import me.matrix4f.cardcutter.prefs.Prefs
import me.matrix4f.cardcutter.ui


class WelcomeWindow2 : ModalWindow("How do I use Cardify?") {

    override fun close(event: WindowEvent?) {
        super.close(event)
        SignInWindow(SignInLauncherOptions.WELCOME, ui!!.currentUser).show()
    }

    override fun generateUI(): Scene {
        val box = VBox()
        box.spacing = 5.0
        box.padding = Insets(10.0)

        val header = Label("How do I use Cardify?")
        header.font = Font.font(18.0)

        val subheader = Label("In your web browser, simply click the Cardify icon to turn a webpage into your next card.")
        subheader.isWrapText = true
        subheader.font = Font.font(14.0)

        val imageView = ImageView(javaClass.getResource("/tutorial.png").toExternalForm())
        imageView.fitWidth = 550.0
        imageView.fitHeight = 293.0
        val continueBtn = Button("I'm ready! \u2192")
        continueBtn.setOnAction {
            close(null)
        }

        box.children.add(header)
        box.children.add(subheader)
        box.children.add(imageView)
        box.children.add(continueBtn)

        val scene = Scene(box, WIDTH, HEIGHT)
        scene.stylesheets.add(javaClass.getResource(Prefs.get().getStylesheet()).toExternalForm())
        super.window.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))
        return scene
    }

    companion object {
        const val WIDTH = 600.0
        const val HEIGHT = 400.0
    }
}