package me.matrix4f.cardcutter.prefs.windows

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.web.WebView
import me.matrix4f.cardcutter.prefs.Prefs
import me.matrix4f.cardcutter.prefs.PrefsObject
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.FileWriter
import java.io.PrintWriter

class CitePrefsWindow: PrefsWindow("Settings - Card Format") {

    private val restOfCite = TextField(Prefs.get().citeFormat)

    override fun close() {
        Prefs.get().citeFormat = restOfCite.text
        Prefs.save()
        super.close()
    }

    override fun generateUI(): Scene {
        val vbox = VBox()
        vbox.padding = Insets(10.0)
        vbox.spacing = 10.0

        val header = Label("Citation")
        header.font = Font.font(20.0)
//        header.style = "-fx-font-weight: bold;"

        val resetBtn = Button("Reset to Default")
        val infoBtn = Button("Macro List")
        val btnHbox = HBox()
        btnHbox.spacing = 10.0
        btnHbox.children.add(resetBtn)
        btnHbox.children.add(infoBtn)

        val authorNameAndDateText = "LastName, DateShort "
        val authorNameAndDate = TextField(authorNameAndDateText)
        authorNameAndDate.isEditable = false
        authorNameAndDate.prefColumnCount = authorNameAndDateText.length+1
        authorNameAndDate.padding = Insets(1.0)
        authorNameAndDate.style = HOLLOW_TEXTFIELD_STYLE.replace("FONTSIZE", "16.0")

        restOfCite.text = Prefs.get().citeFormat
        restOfCite.prefWidth = 600.0
        restOfCite.padding = Insets(1.0)
        restOfCite.border = Border(BorderStroke(Color.web("#ddd"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT))
        restOfCite.style = "-fx-font-size: 14.0;-fx-font-family: 'Calibri';-fx-background-color: '#f4f4f4';"

        val editHBox = HBox()
        editHBox.children.add(authorNameAndDate)
        editHBox.children.add(restOfCite)

        editHBox.prefWidth = 600.0

        resetBtn.setOnAction { restOfCite.text = PrefsObject.DEFAULT_CITE_FORMAT }
        infoBtn.setOnAction {
            val alert = Alert(Alert.AlertType.NONE)
            alert.title = "Macros"
            alert.headerText = "Available Macros"
            alert.buttonTypes.add(ButtonType.CLOSE)

            val list = ListView<String>(FXCollections.observableArrayList(
                "<Author>",
                "<Qualifications>",
                "<Date>",
                "<CurrentDate>",
                "<Publication>",
                "<Title>",
                "<Url>"
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
            close()
        }

        vbox.children.add(header)
        vbox.children.add(editHBox)
        vbox.children.add(btnHbox)
        vbox.children.add(applyBtn)

        val scene = Scene(vbox, 600.0, 150.0)

        return scene
    }

    companion object {
        const val HOLLOW_TEXTFIELD_STYLE = "-fx-font-weight: bold; -fx-font-family: 'Calibri';-fx-font-size: FONTSIZE;-fx-background-color: '#f4f4f4';-fx-faint-focus-color: transparent;-fx-focus-color: transparent;"
    }
}