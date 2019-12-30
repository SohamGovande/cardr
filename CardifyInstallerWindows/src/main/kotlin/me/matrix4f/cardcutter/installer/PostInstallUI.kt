package me.matrix4f.cardcutter.installer

import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import java.awt.Desktop
import java.net.URL

class PostInstallUI {

    fun initialize(): Scene {
        val box = VBox()
        box.style = "-fx-background-color:#fafafa;"
        box.spacing = 5.0
        box.padding = Insets(10.0)

        val header = Label("Successfully installed Cardify. What's next?")
        header.font = Font.font(18.0)
        header.textAlignment = TextAlignment.CENTER

        val subheader = Label("Get the Cardify Chrome extension.")
        subheader.font = Font.font(14.0)
        subheader.textAlignment = TextAlignment.CENTER

        val link = Hyperlink("Download on the Chrome Web Store for free.")
        link.setOnAction {
            Desktop.getDesktop().browse(URL("https://chrome.google.com/webstore/detail/cardifydebate/ifdnjffggmmjiammdpklgldliaaempce").toURI());
        }
        link.font = Font.font(14.0)
        link.textAlignment = TextAlignment.CENTER

        val imageView = ImageView(javaClass.getResource("/icon-128.png").toExternalForm())
        val closeBtn = Button("Close Installer")
        closeBtn.setOnAction { System.exit(0) }

        box.children.add(header)
        box.children.add(subheader)
        box.children.add(link)
        box.children.add(imageView)
        box.children.add(closeBtn)


        val scene = Scene(box, CardifyInstaller.WIDTH, CardifyInstaller.HEIGHT)
        scene.stylesheets.add(javaClass.getResource("/CCStyles.css").toExternalForm());
        return scene
    }
}