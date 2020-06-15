package me.sohamgovande.cardr.core.ui.windows

import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import me.sohamgovande.cardr.data.prefs.Prefs

class CreditsWindow : ModalWindow("Credits") {

    private fun boldText(str: String): Text {
        val text = Text(str)
        text.styleClass.add("custom-text")
        text.style = "-fx-font-weight: bold;"
        text.font = Font.font(13.0)
        return text
    }

    private fun text(str: String): Text {
        val text = Text(str)
        text.styleClass.add("custom-text")
        text.font = Font.font(13.0)
        return text
    }

    override fun generateUI(): Scene {
        val vbox = VBox()
        vbox.spacing = 5.0
        vbox.padding = Insets(10.0)

        val header = Label("Credits")
        header.font = Font.font(20.0)

        val founderAndDeveloper = TextFlow(
            boldText("Founder and Developer: "),
            text("Soham Govande")
        )

        val contactUs = TextFlow(
            boldText("Contact: "),
            text("sohamthedeveloper@gmail.com")
        )

        val libraries = TextFlow(
            boldText("Libraries: "),
            text("Apache Commons-Exec 1.3, " +
            "Apache Commons-IO 2.5, " +
            "Apache HttpClient 4.5.10, " +
            "Apache Log4j 2.13.0, " +
            "Gson 2.8.5, " +
            "Nlohmann Json 3.7.3, " +
            "Jsoup 1.12.1, " +
            "Zip4j 2.3.0")
        )

        vbox.children.add(header)
        vbox.children.add(founderAndDeveloper)
        vbox.children.add(contactUs)
        vbox.children.add(libraries)

        val scene = Scene(vbox, 300.0, 200.0)
        scene.stylesheets.add(javaClass.getResource(Prefs.get().getStylesheet()).toExternalForm())
        super.window.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))
        return scene
    }

}