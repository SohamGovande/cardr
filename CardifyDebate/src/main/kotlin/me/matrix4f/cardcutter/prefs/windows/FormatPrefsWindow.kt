package me.matrix4f.cardcutter.prefs.windows

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.web.HTMLEditor
import javafx.stage.WindowEvent
import me.matrix4f.cardcutter.prefs.Prefs
import me.matrix4f.cardcutter.prefs.PrefsObject
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class FormatPrefsWindow: ModalWindow("Settings - Card Format") {

    private val editText = HTMLEditor()

    override fun close(event: WindowEvent?) {
        Prefs.get().cardFormat = editText.htmlText.replace("contenteditable=\"true\"","")
        Prefs.save()
        super.close(event)
    }

    override fun generateUI(): Scene {
        val vbox = VBox()
        vbox.padding = Insets(10.0)
        vbox.spacing = 10.0

        val header = Label("Card Format")
        header.style = "-fx-font-family: 'Calibri';"
        header.font = Font.font(20.0)

        val resetBtn = Button("Reset to Default")
        val infoBtn = Button("Macro List")
        val btnHbox = HBox()
        btnHbox.spacing = 10.0
        btnHbox.children.add(resetBtn)
        btnHbox.children.add(infoBtn)

        editText.htmlText = Prefs.get().cardFormat
        editText.prefWidth = 600.0
        editText.prefHeight = 400.0
        editText.padding = Insets(1.0)
        editText.border = Border(BorderStroke(Color.web("#ddd"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT))
        editText.style = "-fx-font-size: 14.0;-fx-font-family: 'Calibri';-fx-background-color: '#f4f4f4';"

        val editHBox = HBox()
        editHBox.children.add(editText)

        editHBox.prefWidth = 600.0

        resetBtn.setOnAction { editText.htmlText = PrefsObject.DEFAULT_CARD_FORMAT }
        infoBtn.setOnAction {
            val alert = Alert(Alert.AlertType.NONE)
            alert.title = "Macros"
            alert.headerText = "Available Macros"
            alert.buttonTypes.add(ButtonType.CLOSE)

            val list = ListView<String>(FXCollections.observableArrayList(
                "{AuthorLastName}",
                "{AuthorFullName}",
                "{DateFull}",
                "{DateShortened}",
                "{Qualifications}",
                "{CurrentDate}",
                "{Publication}",
                "{Title}",
                "{Url}"
            ))

            val copyBtn = Button("Copy")
            copyBtn.prefWidth = 250.0
            copyBtn.setOnAction {
                if (list.selectionModel.selectedIndex != -1) {
                    Toolkit.getDefaultToolkit().systemClipboard.setContents(
                        StringSelection(list.selectionModel.selectedItem),
                        null
                    )
                    alert.close()
                }
            }

            val view = VBox()
            view.children.add(list)
            view.children.add(copyBtn)

            list.prefHeight = 150.0
            alert.dialogPane.content = view
            alert.show()
        }

        val applyBtn = Button("Apply")
        applyBtn.requestFocus()
        applyBtn.setOnAction {
            close(null)
        }

        vbox.children.add(header)
        vbox.children.add(editHBox)
        vbox.children.add(btnHbox)
        vbox.children.add(applyBtn)

        val scene = Scene(vbox, 600.0, 400.0)
        scene.stylesheets.add(javaClass.getResource("/CCStyles.css").toExternalForm())
        return scene
    }

    companion object {
        const val HOLLOW_TEXTFIELD_STYLE = "-fx-font-weight: bold; -fx-font-family: 'Calibri';-fx-font-size: FONTSIZE;-fx-background-color: '#f4f4f4';-fx-faint-focus-color: transparent;-fx-focus-color: transparent;"
    }
}