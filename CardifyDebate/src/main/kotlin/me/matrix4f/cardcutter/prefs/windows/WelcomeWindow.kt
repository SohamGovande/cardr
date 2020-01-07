package me.matrix4f.cardcutter.prefs.windows

import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import me.matrix4f.cardcutter.ui
import java.awt.Desktop
import java.net.URL


class WelcomeWindow : ModalWindow("Welcome to Cardify!") {

    init {
        window.initStyle(StageStyle.UTILITY)
    }

    override fun close(event: WindowEvent?) {
        super.close(event)
        SignInWindow(SignInLauncherOptions.WELCOME, ui.currentUser).show()
    }

    override fun generateUI(): Scene {
        val box = VBox()
        box.style = "-fx-background-color:#f4f4f4;"
        box.spacing = 5.0
        box.padding = Insets(10.0)

        val header = Label("Welcome to Cardify! What's next?")
        header.font = Font.font(18.0)
        header.textAlignment = TextAlignment.CENTER

        val subheader = Label("Get the CardifyDebate Chrome extension.")
        subheader.font = Font.font(14.0)
        subheader.textAlignment = TextAlignment.CENTER

        val link = Hyperlink("Download on the Chrome Web Store for free.")
        link.setOnAction {
            Desktop.getDesktop().browse(URL("https://chrome.google.com/webstore/detail/cardifydebate/ifdnjffggmmjiammdpklgldliaaempce").toURI());
        }
        link.font = Font.font(14.0)
        link.textAlignment = TextAlignment.CENTER

        val imageView = ImageView(javaClass.getResource("/icon-128.png").toExternalForm())
        val continueBtn = Button("Continue to Sign-In \u2192")
        continueBtn.setOnAction {
            close(null)
        }

        box.children.add(header)
        box.children.add(subheader)
        box.children.add(link)
        box.children.add(imageView)
        box.children.add(continueBtn)

        val scene = Scene(box, WIDTH, HEIGHT)
        scene.stylesheets.add(javaClass.getResource("/styles.css").toExternalForm());
        return scene
    }

    companion object {
        const val WIDTH = 400.0
        const val HEIGHT = 265.0
    }
}