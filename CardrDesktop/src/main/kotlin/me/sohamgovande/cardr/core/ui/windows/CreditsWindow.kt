package me.sohamgovande.cardr.core.ui.windows

import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.data.urls.UrlHelper
import java.awt.Desktop
import java.net.URL

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

    private fun link(str: String, href: String): Hyperlink {
        val link = Hyperlink(str)
        link.setOnAction { Desktop.getDesktop().browse(URL(href).toURI()) }
        return link
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
            text("Apache Commons-Exec, " +
                "Apache Commons-IO, " +
                "Apache HttpClient, " +
                "Apache Log4j, " +
                "Google Gson, " +
                "JSoup, " +
                "Nlohmann Json, " +
                "NSMenuFX" +
                "Tess4j, " +
                "Zip4j" +
                ""
            )
        )
        libraries.prefWidth = 275.0

        val icons = TextFlow(boldText("Tools Icon Credits: "), link("Icons8", UrlHelper.get("icons8")))

        val closeBtn = Button("Close")
        closeBtn.prefWidth = 275.0
        closeBtn.setOnAction { close(null) }

        vbox.children.add(header)
        vbox.children.add(founderAndDeveloper)
        vbox.children.add(contactUs)
        vbox.children.add(libraries)
        vbox.children.add(icons)
        vbox.children.add(closeBtn)

        val scene = Scene(vbox)
        scene.stylesheets.add(javaClass.getResource(Prefs.get().getStylesheet()).toExternalForm())
        super.window.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))
        return scene
    }

}