package me.matrix4f.cardcutter.ui.windows

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.web.WebView
import me.matrix4f.cardcutter.data.prefs.Prefs
import java.awt.Desktop
import java.net.URL


class HistoryWindow: ModalWindow("Card History") {

    private val webView = WebView()

    init {
        window.widthProperty().addListener {_, _, _ -> onWindowResized()}
        window.heightProperty().addListener {_, _, _ -> onWindowResized()}
    }

    override fun generateUI(): Scene {
        val vbox = VBox()
        vbox.padding = Insets(10.0)
        vbox.spacing = 10.0

        val header = Label("Card History")
        header.font = Font.font(20.0)

        webView.engine.load("http://cardifydebate.x10.bz/history.php?email=${Prefs.get().emailAddress}&token=${Prefs.get().accessToken}&dark_mode=${Prefs.get().darkMode}")
        webView.engine.locationProperty().addListener { _, oldLoc, loc ->
            if (loc != null && !loc.contains("cardifydebate.x10.bz")) {
                Platform.runLater { webView.engine.load(oldLoc) }
            } else {
                Desktop.getDesktop().browse(URL(oldLoc).toURI())
            }
        }

        vbox.children.add(header)
        vbox.children.add(webView)

        val scene = Scene(vbox, 600.0, 400.0)
        scene.stylesheets.add(javaClass.getResource(Prefs.get().getStylesheet()).toExternalForm())
        return scene
    }

    private fun onWindowResized() {
        webView.prefWidth = window.width
    }
}