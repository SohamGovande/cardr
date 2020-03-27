package me.sohamgovande.cardr.core.ui.windows

import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import javafx.stage.WindowEvent
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.data.urls.UrlHelper
import java.awt.Desktop
import java.net.URL


class WelcomeWindow : ModalWindow("Welcome to cardr!") {

    var linkOpened = false

    override fun close(event: WindowEvent?) {
        super.close(event)
        if (!forcedClose)
            WelcomeWindow2().show()
    }

    override fun generateUI(): Scene {
        val box = VBox()
        box.spacing = 5.0
        box.padding = Insets(10.0)

        val header = Label("Welcome to cardr! What's next?")
        header.font = Font.font(18.0)
        header.textAlignment = TextAlignment.CENTER

        val subheader = Label("Get the cardr Chrome Extension.")
        subheader.font = Font.font(14.0)
        subheader.textAlignment = TextAlignment.CENTER

        val link = Hyperlink("Click here to get it for free.")
        link.setOnAction {
            UrlHelper.browse("extension")
            linkOpened = true
        }
        link.font = Font.font(14.0)
        link.textAlignment = TextAlignment.CENTER

        val imageView = ImageView(javaClass.getResource("/icon-128.png").toExternalForm())
        val continueBtn = Button("Continue \u2192")
        continueBtn.setOnAction {
            if (!linkOpened) {
                link.fire()
            }
            close(null)
        }

        box.children.add(header)
        box.children.add(subheader)
        box.children.add(link)
        box.children.add(imageView)
        box.children.add(continueBtn)

        val scene = Scene(box, WIDTH, HEIGHT)
        scene.stylesheets.add(javaClass.getResource(Prefs.get().getStylesheet()).toExternalForm())
        super.window.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))
        return scene
    }

    companion object {
        const val WIDTH = 400.0
        const val HEIGHT = 265.0
    }
}