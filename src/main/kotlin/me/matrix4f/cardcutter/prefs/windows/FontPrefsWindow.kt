package me.matrix4f.cardcutter.prefs.windows

import javafx.beans.value.ObservableValue
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import me.matrix4f.cardcutter.prefs.Prefs

class FontPrefsWindow: PrefsWindow("Settings - Font") {

    private val fontTF = TextField("Calibri")
    private val sizeTF = TextField("11")

    override fun close() {
        Prefs.get().fontName = fontTF.font.name
        Prefs.get().fontSize = sizeTF.font.size.toInt()
        Prefs.save()

        super.close()
    }

    override fun generateUI(): Scene {
        val vbox = VBox()
        vbox.spacing = 5.0
        vbox.padding = Insets(10.0)

        val gp = GridPane()
        gp.hgap = 5.0
        gp.vgap = 5.0
        gp.padding = Insets(10.0)

        fontTF.textProperty().addListener { _: ObservableValue<out String>, _: String, newValue: String -> Unit
            val font = Font.font(newValue, sizeTF.text.toDouble())
            if (font.name.equals(newValue, true)) {
                fontTF.font = font
                sizeTF.font = font
            }
        }

        sizeTF.textProperty().addListener { _: ObservableValue<out String>, _: String, newValue: String -> Unit
            var font: Font
            try {
                font = Font.font(fontTF.font.name, newValue.toDouble())
            } catch (e: Exception) {
                var adjustedFontSize = newValue.replace(Regex("[^\\d]"), "")

                sizeTF.text = adjustedFontSize
                if (adjustedFontSize.isEmpty())
                    adjustedFontSize = "11"

                font = Font.font(
                    fontTF.font.name,
                    adjustedFontSize.toDouble()
                )
            }

            fontTF.font = font
            sizeTF.font = font
        }

        fontTF.text = Prefs.get().fontName
        sizeTF.text = Prefs.get().fontSize.toString()

        gp.add(Label("Family"), 0, 0)
        gp.add(fontTF, 1, 0)

        gp.add(Label("Size"), 0, 1)
        gp.add(sizeTF, 1, 1)

        val header = Label("Font Settings")
        header.font = Font.font(20.0)
        header.style = "-fx-font-weight: bold;"
        val subheader = Label("NOTE: For the \"Send to Verbatim\" feature")

        val headerCardBody = Label("Card Body")
        headerCardBody.style = "-fx-font-weight: bold;"
        val headerTagAndCite = Label("Tag and Cite")
        headerTagAndCite.style = "-fx-font-weight: bold;"

        val applyBtn = Button("Apply")
        applyBtn.prefWidth = 300.0
        applyBtn.setOnAction {
            close()
        }

        vbox.children.add(header)
        vbox.children.add(subheader)
        vbox.children.add(headerTagAndCite)
        vbox.children.add(Label("This can only be changed from within Verbatim."))
        vbox.children.add(headerCardBody)
        vbox.children.add(gp)
        vbox.children.add(applyBtn)

        val scene = Scene(vbox, 300.0, 250.0)
        return scene
    }
}