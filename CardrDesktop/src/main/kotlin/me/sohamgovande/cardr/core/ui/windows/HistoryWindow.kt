package me.sohamgovande.cardr.core.ui.windows

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.web.WebView
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.data.urls.UrlHelper
import java.awt.Desktop
import java.net.URL


class HistoryWindow(private val cardrUI: CardrUI): ModalWindow("Card History") {

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

        val onClickHbox = HBox()

        val onClickLbl = Label("When opening a link, ")
        onClickLbl.font = Font.font(15.0)
        val onClickCombo = ComboBox(FXCollections.observableList(listOf(
            "open it in Cardr",
            "open it in a web browser"
        )))

        onClickHbox.children.add(onClickLbl)
        onClickHbox.children.add(onClickCombo)
        onClickHbox.children.add(Label("."))

        if (!Prefs.get().openHistoryWithinCardr) {
            onClickCombo.selectionModel.select(1)
        } else {
            onClickCombo.selectionModel.select(0)
        }

        onClickCombo.selectionModel.selectedIndexProperty().addListener { _, _, value ->
            Prefs.get().openHistoryWithinCardr = value == 0
            Prefs.save()
        }

        webView.engine.load(generateURL())

        var disableNext = false
        webView.engine.locationProperty().addListener { _, oldLoc, loc ->
            if (disableNext) {
                disableNext = false
                return@addListener
            }

            if (loc != null && loc.contains("cardr")) {
                Platform.runLater { webView.engine.load(oldLoc) }
            } else if (!loc.contains("cardr")){
                if (onClickCombo.selectionModel.selectedIndex == 0) {
                    close(null)
                    cardrUI.urlTF.text = loc
                    cardrUI.gotoUrlButton.fire()
                } else {
                    Desktop.getDesktop().browse(URL(loc).toURI())
                    Platform.runLater { webView.engine.load(oldLoc) }
                    disableNext = true
                }
            }
        }

        vbox.children.add(header)
        vbox.children.add(onClickHbox)
        vbox.children.add(webView)

        val scene = Scene(vbox, 600.0, 400.0)
        scene.stylesheets.add(javaClass.getResource(Prefs.get().getStylesheet()).toExternalForm())
        super.window.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))
        return scene
    }

    private fun generateURL(): String = "${UrlHelper.get("history")}?email=${Prefs.get().emailAddress}&token=${Prefs.get().accessToken}&dark_mode=${Prefs.get().darkMode}"

    private fun onWindowResized() {
        webView.prefWidth = window.width
    }
}