package me.sohamgovande.cardr.core.ui.windows

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.WindowEvent
import me.sohamgovande.cardr.data.prefs.Prefs
import java.awt.event.KeyEvent

class SendToWordSettingsWindow : ModalWindow("Settings - Send to Word") {

    private lateinit var plainTextRB: RadioButton
    private lateinit var htmlTextRB: RadioButton
    private lateinit var tg: ToggleGroup


    private lateinit var pasteShortcutKeyCB: ComboBox<String>

    override fun close(event: WindowEvent?) {
        if (!forcedClose) {
            Prefs.get().pastePlainText = plainTextRB.isSelected
            Prefs.get().pasteShortcut = pasteShortcutKeyCB.selectionModel.selectedIndex + KeyEvent.VK_F1
            Prefs.save()
        }
        super.close(event)
    }

    override fun generateUI(): Scene {
        val vbox = VBox()
        vbox.padding = Insets(10.0)
        vbox.spacing = 10.0

        val header = Label("Send to Word Settings")
        header.font = Font.font(20.0)

        val subheader = Label("These settings only affect the 'Send to Word' functionality and no other functions (e.g., Copy Card to Clipboard).")
        subheader.isWrapText = true

        val details = Label("If you select PLAIN text paste, cardr will try to use (1) Verbatim's Paste Without Formatting option for the card body and (2) HTML for the card header. On the other hand, HTML text paste directly transfers the entire card in HTML. The PLAIN text paste option is preferable because, once the card is pasted, PLAIN pasted text is directly editable through Verbatim underline/emphasize. The PLAIN text feature is currently in beta; if you are experiencing any problems, feel free to revert to HTML text, which was the only option in cardr V1.1.0 and below.")
        details.isWrapText = true

        tg = ToggleGroup()
        plainTextRB = RadioButton("Send card body to Word as PLAIN text (new in v1.2.0)")
        plainTextRB.toggleGroup = tg
        htmlTextRB = RadioButton("Send card body to Word as HTML text (default option in v1.1.0 and below)")
        htmlTextRB.toggleGroup = tg

        if (Prefs.get().pastePlainText) {
            htmlTextRB.isSelected = false
            plainTextRB.isSelected = true
        } else {
            plainTextRB.isSelected = false
            htmlTextRB.isSelected = true
        }

        val saveBtn = Button("Save")
        saveBtn.requestFocus()
        saveBtn.setOnAction {
            close(null)
        }

        val keyHbox = HBox()
        keyHbox.spacing = 5.0
        pasteShortcutKeyCB = ComboBox()
        pasteShortcutKeyCB.items = FXCollections.observableList(listOf(
            "F1",
            "F2",
            "F3",
            "F4",
            "F5",
            "F6",
            "F7",
            "F8",
            "F9",
            "F10",
            "F11",
            "F12",
            "F13",
            "F14",
            "F15",
            "F16",
            "F17",
            "F18",
            "F19",
            "F20",
            "F21",
            "F22",
            "F23",
            "F24"
        ))
        pasteShortcutKeyCB.selectionModel.select(
            Prefs.get().pasteShortcut - KeyEvent.VK_F1
        )
        keyHbox.children.add(Label("Verbatim 'paste without formatting' shortcut (for PLAIN text only):"))
        keyHbox.children.add(pasteShortcutKeyCB)

        vbox.children.add(header)
        vbox.children.add(subheader)
        vbox.children.add(details)
        vbox.children.add(plainTextRB)
        vbox.children.add(htmlTextRB)
        vbox.children.add(keyHbox)
        vbox.children.add(saveBtn)

        val scene = Scene(vbox, 600.0, 400.0)
        scene.stylesheets.add(javaClass.getResource(Prefs.get().getStylesheet()).toExternalForm())
        super.window.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))
        return scene
    }
}