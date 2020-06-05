package me.sohamgovande.cardr.core.ui.windows

import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.input.KeyEvent
import javafx.scene.layout.FlowPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.web.WebView
import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.data.prefs.Prefs

class MarkupCardWindow(private val cardrUI: CardrUI, private val cardBodyHTML: String): ModalWindow("Highlight & Underline Card") {

    private val optionsPane = FlowPane()

    private val clearBtn = Button("Reset All")
    private val boldBtn = Button("Bold")
    private val underlineBtn = Button("Underline")
    private val emphasizeBtn = Button("Emphasize")
    private val highlightBtn = Button("Highlight")
    private val eraserBtn = Button("Unhighlight")
    private val settingsBtn = Button()

    private val cardWV = WebView()

    private val applyChangesBtn = Button("Apply Changes")
    private val discardChangesBtn = Button("Discard & Exit")
    var applyChanges = false

    override fun generateUI(): Scene {
        val vbox = VBox()
        vbox.padding = Insets(10.0)
        vbox.spacing = 10.0

        optionsPane.hgap = 5.0
        optionsPane.vgap = 5.0

        optionsPane.children.addAll(clearBtn, boldBtn, underlineBtn, emphasizeBtn, highlightBtn, eraserBtn, settingsBtn)

        discardChangesBtn.setOnAction {
            close(null)
        }

        applyChangesBtn.setOnAction {
            applyChanges = true
            onCloseData["cardBody"] = cardWV.engine.executeScript("document.documentElement.outerHTML") as String
            window.onCloseRequest.handle(null)
        }

        val exitHBox = HBox()
        exitHBox.spacing = 10.0
        exitHBox.children.addAll(
            discardChangesBtn,
            applyChangesBtn
        )

        cardWV.setOnKeyPressed(this::onKeyPressed)
        cardWV.prefWidth = WIDTH - 50
        cardWV.prefHeight = CardrDesktop.HEIGHT - 100
        cardWV.engine.loadContent(cardBodyHTML)

        clearBtn.setOnAction { cardWV.engine.loadContent(cardBodyHTML) }
        boldBtn.setOnAction { boldSelectedText(); clearSelection() }
        underlineBtn.setOnAction { underlineSelectedText(); clearSelection() }
        highlightBtn.setOnAction { highlightSelectedText(); clearSelection() }
        eraserBtn.setOnAction { unhighlightSelectedText(); clearSelection() }
        emphasizeBtn.setOnAction { underlineSelectedText(); boldSelectedText(); clearSelection() }
        settingsBtn.setOnAction { MarkupCardSettingsWindow().show() }

        vbox.children.add(optionsPane)
        vbox.children.add(cardWV)
        vbox.children.add(exitHBox)
        loadMenuIcons()

        val scene = Scene(vbox, WIDTH, HEIGHT)
        scene.stylesheets.add(javaClass.getResource(Prefs.get().getStylesheet()).toExternalForm())
        super.window.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))
        return scene
    }

    private fun onKeyPressed(event: KeyEvent) {
        when(event.code.code) {
            Prefs.get().boldShortcut -> boldSelectedText()
            Prefs.get().underlineShortcut -> underlineSelectedText()
            Prefs.get().emphasizeShortcut -> {
                boldSelectedText(); underlineSelectedText()
            }
            Prefs.get().highlightShortcut -> highlightSelectedText()
            Prefs.get().unhighlightShortcut -> unhighlightSelectedText()
        }
        clearSelection()
    }

    private fun highlightSelectedText() {
        cardWV.engine.executeScript("highlightSelectedText('${Prefs.get().highlightColor}')")
    }

    private fun unhighlightSelectedText() {
        cardWV.engine.executeScript("highlightSelectedText('#00000000')")
    }

    private fun boldSelectedText() {
        cardWV.engine.executeScript("boldSelectedText()")
    }

    private fun underlineSelectedText() {
        cardWV.engine.executeScript("underlineSelectedText()")
    }

    private fun clearSelection() {
        cardWV.engine.executeScript("clearSelection()")
    }

    fun loadMenuIcons() {
        highlightBtn.graphic = cardrUI.loadMiniIcon("/highlight.png")
        emphasizeBtn.graphic = cardrUI.loadMiniIcon("/emphasize.png")
        underlineBtn.graphic = cardrUI.loadMiniIcon("/underline.png")
        boldBtn.graphic = cardrUI.loadMiniIcon("/bold.png")
        eraserBtn.graphic = cardrUI.loadMiniIcon("/eraser.png")
        clearBtn.graphic = cardrUI.loadMiniIcon("/reset-all.png")
        settingsBtn.graphic = cardrUI.loadMiniIcon("/settings.png")
    }

    companion object {
        const val WIDTH = 600.0
        const val HEIGHT = 400.0
    }
}