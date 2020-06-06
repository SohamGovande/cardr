package me.sohamgovande.cardr.core.ui.windows.ocr

import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.WindowEvent
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.core.ui.windows.ModalWindow
import me.sohamgovande.cardr.data.prefs.Prefs

class OCRCardBuilderWindow(private val cardrUI: CardrUI) : ModalWindow("OCR Card Builder") {

    private val textArea = TextArea()

    override fun generateUI(): Scene {
        val vbox = VBox()
        vbox.spacing = 10.0
        vbox.padding = Insets(10.0)

        val header = Label("OCR Card Builder")
        header.font = Font.font(20.0)

        textArea.isWrapText = true
        textArea.prefWidth = WIDTH
        textArea.prefHeight = 1000.0

        textArea.setOnKeyTyped {
            if (it.character == "\r" || it.character == "\n" || it.character == "\r\n") {
                textArea.insertText(textArea.caretPosition, "\n")
            }
        }

        val addMoreTextBtn = Button("Add more OCR text")
        addMoreTextBtn.graphic = cardrUI.loadMiniIcon("/capture-ocr.png", false, 1.0)
        addMoreTextBtn.setOnAction {
            OCRSelectionWindow(cardrUI).show()
        }

        val discardBtn = Button("Discard & Exit")
        discardBtn.setOnAction { close(null) }

        val applyBtn = Button("Finish importing")
        applyBtn.setOnAction {
            onCloseData["ocrText"] = textArea.text
            window.onCloseRequest.handle(null)
        }

        val closeHbox = HBox()
        closeHbox.spacing = 10.0
        closeHbox.children.addAll(discardBtn, applyBtn)

        vbox.children.addAll(
            header,
            addMoreTextBtn,
            textArea,
            closeHbox
        )

        discardBtn.graphic = cardrUI.loadMiniIcon("/close.png", false, 1.0)
        applyBtn.graphic = cardrUI.loadMiniIcon("/checkmark.png", false, 1.0)

        val scene = Scene(vbox, WIDTH, HEIGHT)
        scene.stylesheets.add(javaClass.getResource(Prefs.get().getStylesheet()).toExternalForm())
        super.window.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))
        return scene
    }

    fun importText(text: String) {
        var formatted = text.replace("\n"," ").replace("\r"," ") + "\n\n"
        while (formatted.contains("  "))
            formatted = formatted.replace("  ", " ")
        val caretPosition = textArea.caretPosition
        textArea.insertText(caretPosition, formatted)
        textArea.requestFocus()
    }

    override fun close(event: WindowEvent?) {
        super.close(event)
        cardrUI.ocrCardBuilderWindow = null
    }

    companion object {
        const val WIDTH = 600.0
        const val HEIGHT = 500.0
    }
}