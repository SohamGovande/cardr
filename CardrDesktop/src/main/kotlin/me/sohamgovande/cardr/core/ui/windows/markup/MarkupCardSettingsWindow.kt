package me.sohamgovande.cardr.core.ui.windows.markup

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import me.sohamgovande.cardr.core.ui.windows.ModalWindow
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.data.prefs.PrefsObject
import java.awt.event.KeyEvent

class MarkupCardSettingsWindow : ModalWindow("Highlight & Underline Settings") {

    override fun generateUI(): Scene {
        val vbox = VBox()
        vbox.padding = Insets(10.0)
        vbox.spacing = 10.0

        val header = Label("Highlight & Underline")
        header.font = Font.font(20.0)

        val doneBtn = Button("Done")
        doneBtn.prefWidth = WIDTH - 20.0
        doneBtn.setOnAction { window.onCloseRequest.handle(null) }

        vbox.children.addAll(listOf(
            header,
            generateShortcut("Bold", Prefs.get().boldShortcut) {
                Prefs.get().boldShortcut = it
            },
            generateShortcut("Underline", Prefs.get().underlineShortcut) {
                Prefs.get().underlineShortcut = it
            },
            generateShortcut("Emphasize", Prefs.get().emphasizeShortcut) {
                Prefs.get().emphasizeShortcut = it
            },
            generateShortcut("Highlight", Prefs.get().highlightShortcut) {
                Prefs.get().highlightShortcut = it
            },
            generateShortcut("Unhighlight", Prefs.get().unhighlightShortcut) {
                Prefs.get().unhighlightShortcut = it
            },
            generateHighlightRow(),
            doneBtn
        ))

        val scene = Scene(vbox, WIDTH, HEIGHT)
        scene.stylesheets.add(javaClass.getResource(Prefs.get().getStylesheet()).toExternalForm())
        super.window.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))
        return scene
    }

    private fun generateHighlightRow(): HBox {
        val hbox = HBox()
        val lbl = Label("Highlight Color")
        lbl.minWidth = 100.0

        val colorCB = ComboBox(FXCollections.observableList(PrefsObject.COLOR_MAP.keys.toList()))
        colorCB.selectionModel.select(PrefsObject.COLOR_MAP.entries.filter {
            it.value == Prefs.get().highlightColor
        }.map { it.key }.firstOrNull() ?: "Yellow")
        lbl.background = Background(BackgroundFill(Color.web(Prefs.get().highlightColor), CornerRadii.EMPTY, Insets(0.0, 15.0, 0.0, 0.0)))

        colorCB.selectionModel.selectedItemProperty().addListener { _, _, value ->
            Prefs.get().highlightColor = PrefsObject.COLOR_MAP[value] ?: "#ffff00"
            Prefs.save()
            lbl.background = Background(BackgroundFill(Color.web(Prefs.get().highlightColor), CornerRadii.EMPTY, Insets(0.0, 15.0, 0.0, 0.0)))
        }

        hbox.children.add(lbl)
        hbox.children.add(colorCB)

        return hbox
    }

    private fun generateShortcut(name: String, initialSelection: Int, setter: (Int) -> Unit): HBox {
        val hbox = HBox()
        val lbl = Label(name)
        lbl.minWidth = 100.0

        val shortcutCB = ComboBox(FXCollections.observableList(FUNCTION_KEYS))
        shortcutCB.selectionModel.select(initialSelection - KeyEvent.VK_F1)
        shortcutCB.selectionModel.selectedIndexProperty().addListener { _, _, value ->
            val offset = shortcutCB.selectionModel.selectedIndex
            setter(if (offset == -1) initialSelection else value.toInt() + KeyEvent.VK_F1)
            Prefs.save()
        }

        hbox.children.add(lbl)
        hbox.children.add(shortcutCB)
        return hbox
    }

    companion object {
        const val WIDTH = 225.0
        const val HEIGHT = 300.0
        val FUNCTION_KEYS = listOf(
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
            "F12"
        )
    }
}