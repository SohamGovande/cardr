package me.sohamgovande.cardr.core.ui.tabs

import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import javafx.stage.FileChooser
import javafx.stage.Stage
import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.core.ui.windows.FormatPrefsWindow
import me.sohamgovande.cardr.core.ui.windows.markup.MarkupCardWindow
import me.sohamgovande.cardr.core.ui.windows.ocr.OCRSelectionWindow
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.platformspecific.MacMSWordInteractor
import me.sohamgovande.cardr.platformspecific.WinMSWordInteractor
import me.sohamgovande.cardr.ui
import me.sohamgovande.cardr.util.*
import org.apache.logging.log4j.LogManager
import org.jsoup.Jsoup
import java.awt.Desktop
import java.awt.Toolkit
import java.nio.file.Paths

class ToolsPaneUI(private val currentTab: EditCardTabUI, private val cardrUI: CardrUI) {

    val copyBtn = Button("Copy")
    val removeSelectedBtn = Button("Remove Selected")
    val restoreRemovedBtn = Button("Restore to Original")
    val keepOnlySelectedBtn = Button("Keep Only Selected")
    val editCardFormatBtn = Button("Edit Card Format")
    val markupBtn = Button("Highlight & Underline Card")
    val ocrBtn = Button("OCR Tool")

    val sendToWordUI = VBox()
    val wordWindowList = ComboBox<String>()
    val sendToWordBtn = Button("Send to Word")
    val refreshBtn = Button()

    val root = VBox()

    lateinit var scrollPane: ScrollPane

    private fun generateSendToWordUI() {
        sendToWordUI.spacing = 5.0

        val sendToWordHeader = Label("Send Card to Word")
        sendToWordHeader.style = "-fx-font-weight: bold;"
        sendToWordHeader.prefWidth = 225.0
        sendToWordHeader.textAlignment = TextAlignment.CENTER
        sendToWordUI.children.add(sendToWordHeader)
        sendToWordUI.children.add(Label("Select Word window:"))

        val exportToWordHBox = GridPane()
        exportToWordHBox.hgap = 5.0

        wordWindowList.padding = Insets(0.0, 0.0, 0.0, 10.0)
        exportToWordHBox.add(refreshBtn, 0, 0)
        exportToWordHBox.add(wordWindowList, 1, 0)
        sendToWordUI.children.add(exportToWordHBox)
        wordWindowList.selectionModel.selectedIndexProperty().addListener(this::onSelectedWordWindowChanged)

        sendToWordBtn.setOnAction { sendCardToWord() }
        sendToWordUI.children.add(sendToWordBtn)

        if (getOSType() == OS.WINDOWS) {
            val msWordInteractor = WinMSWordInteractor()
            wordWindowList.items = FXCollections.observableList(msWordInteractor.getValidWordWindows())

            if (!wordWindowList.items.isEmpty()) {
                wordWindowList.selectionModel.select(0)
            }
        } else if (getOSType() == OS.MAC) {
            val msWordInteractor = MacMSWordInteractor()
            wordWindowList.items = FXCollections.observableList(msWordInteractor.getValidWordWindows())
            if (!wordWindowList.items.isEmpty()) {
                wordWindowList.selectionModel.select(0)
            }
        }

        val copyHeader = Label("Copy Card")
        copyHeader.style = "-fx-font-weight: bold;"
        sendToWordUI.children.add(copyHeader)

        copyBtn.setOnAction { copyCardToClipboard() }
        sendToWordUI.children.add(copyBtn)
    }

    fun generateUI(): Node {
        generateSendToWordUI()

        val header = Label("Tools")
        header.font = Font.font(20.0)

        ocrBtn.setOnAction {OCRSelectionWindow.openWindow(ui!!) { currentTab } }
        removeSelectedBtn.setOnAction { removeSelectedText() }
        restoreRemovedBtn.setOnAction {
            currentTab.removeWords.clear()
            currentTab.removeParagraphs.clear()
            currentTab.overrideBodyHTML = null
            currentTab.enableCardBodyEditOptions()
            currentTab.statusBar.text = ""

            currentTab.refreshHTML()
            val alert = Alert(Alert.AlertType.INFORMATION)
            alert.dialogPane.stylesheets.add(CardrDesktop::class.java.getResource(Prefs.get().getStylesheet()).toExternalForm())
            alert.headerText = "Article content restored to original."
            alert.showAndWait()
        }
        keepOnlySelectedBtn.setOnAction { keepOnlySelectedText() }
        editCardFormatBtn.setOnAction { FormatPrefsWindow(cardrUI, cardrUI.getSelectedTab(EditCardTabUI::class.java)!!.propertyManager).show() }
        refreshBtn.setOnAction { refreshWordWindows() }
        markupBtn.setOnAction { openMarkupWindow() }

        initButtonWidths(arrayOf(
            ocrBtn, removeSelectedBtn, restoreRemovedBtn, keepOnlySelectedBtn, editCardFormatBtn, markupBtn,
            sendToWordBtn, copyBtn
        ))

        val textManipPane = VBox()
        textManipPane.spacing = 5.0
        textManipPane.children.addAll(
            restoreRemovedBtn,
            removeSelectedBtn,
            keepOnlySelectedBtn,
            ocrBtn
        )

        val editCardPane = VBox()
        editCardPane.spacing = 5.0
        editCardPane.children.addAll(
            markupBtn,
            editCardFormatBtn
        )

        val sendToWordTP = TitledPane("Export Card", sendToWordUI)
        val textManipTP = TitledPane("Add & Remove Text", textManipPane)
        val editCardTP = TitledPane("Edit Card", editCardPane)
        root.children.add(header)

        val titlePanes = arrayOf(sendToWordTP, textManipTP, editCardTP)
        root.children.addAll(titlePanes)

        for (i in titlePanes.indices) {
            titlePanes[i].expandedProperty().addListener { _, _, value ->
                Prefs.get().toolPanes[i.toString()] = value
                Prefs.save()
            }
        }

        for (entry in Prefs.get().toolPanes) {
            try {
                val index = entry.key.toInt()
                titlePanes[index].isExpanded = entry.value
            } catch (e: Exception) {
                logger.error("Error loading tool pane data", e)
            }
        }

        root.maxWidth = 220.0

        scrollPane = ScrollPane(root)
        scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        scrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        scrollPane.minWidth = 230.0
        scrollPane.style = "-fx-background-color:transparent;"

        return scrollPane
    }

    fun initButtonWidths(buttons: Array<Button>) {
        for (btn in buttons) {
            btn.prefWidth = 200.0
        }
    }

    fun keepOnlySelectedText() {
        var success = false
        try {
            if (currentTab.reader == null)
                return
            currentTab.keepOnlyText(currentTab.cardWV.engine.executeScript("getSelectionTextCustom()") as String)
            success = true
        } catch (e: Exception) {
            logger.error(e)
            e.printStackTrace()
        }

        if (!success) {
            val alert = Alert(Alert.AlertType.INFORMATION, "Please highlight at least one full paragraph in the preview pane in order to use this tool.")
            alert.dialogPane.stylesheets.add(CardrDesktop::class.java.getResource(Prefs.get().getStylesheet()).toExternalForm())
            alert.headerText = "Not enough text selected"
            alert.showAndWait()
        }
    }


    @Suppress("UNUSED_PARAMETER")
    fun onSelectedWordWindowChanged(observable: ObservableValue<out Number>, oldValue: Number, newValue: Number) {
        if (newValue.toInt() < 0 || newValue.toInt() >= wordWindowList.items.size)
            return
        val option = wordWindowList.items[newValue.toInt()]
        if (option == "Create new doc...") {
            if (getOSType() == OS.WINDOWS) {
                val verbatimFile = Paths.get(System.getProperty("user.home"), "AppData", "Roaming", "Microsoft", "Templates", "Debate.dotm").toFile()
                val exeFile = Paths.get("C:\\Program Files (x86)\\Microsoft Office\\root\\Office16\\WINWORD.EXE").toFile()
                if (!exeFile.exists()) {
                    showErrorDialogUnblocking("Unable to launch Word", "No file found at ${exeFile.canonicalPath}.")
                } else {
                    if (verbatimFile.exists()) {
                        executeCommandUnblocking("\"${exeFile.canonicalPath}\" \"/t${verbatimFile.canonicalPath}\"", logger)
                    } else {
                        Desktop.getDesktop().open(exeFile)
                    }
                }
            } else {
                try {
                    MacMSWordInteractor().createNewDoc()
                } catch (e: Exception) {
                    logger.error("Unable to open Microsoft Word", e)
                    showErrorDialogUnblocking("Unable to launch Word", e.javaClass.simpleName + " - " + e.message)
                }
            }
            wordWindowList.selectionModel.select(0)

            Thread {
                Thread.sleep(4000)
                Platform.runLater { refreshWordWindows() }
            }.start()
        } else if (option == "Open doc...") {
            val fileChooser = FileChooser()
            fileChooser.title = "Open Word document..."
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Word documents", "*.docx","*.docm","*.dotx","*.dotm","*.docb","*.doc","*.dot"))
            val selectedFile = fileChooser.showOpenDialog(cardrUI.stage)
            wordWindowList.selectionModel.select(0)
            if (selectedFile != null)
                Desktop.getDesktop().open(selectedFile)

            Thread {
                Thread.sleep(4000)
                Platform.runLater { refreshWordWindows() }
            }.start()
        }
    }

    fun refreshWordWindows() {
        val windows: List<String> = when {
            getOSType() == OS.WINDOWS -> {
                WinMSWordInteractor().getValidWordWindows()
            }
            getOSType() == OS.MAC -> {
                MacMSWordInteractor().getValidWordWindows()
            }
            else -> {
                emptyList()
            }
        }
        Platform.runLater {
            if (!windows.isEmpty()) {
                wordWindowList.items = FXCollections.observableList(windows)
                if (hasWordWindows())
                    wordWindowList.selectionModel.select(0)
            } else {
                initNoWordWindows()
            }
        }
    }

    fun hasWordWindows(): Boolean {
        return !wordWindowList.items[0].equals("No windows open")
    }

    fun initNoWordWindows() {
        wordWindowList.items = FXCollections.observableList(listOf(
            "No windows open",
            "Create new doc...",
            "Open doc..."
        ))
        wordWindowList.selectionModel.select(0)
    }

    private fun openMarkupWindow() {
        val cardBody = currentTab.generateCardBodyHTML(currentTab.cardBody.get(), cardBodyIsHTML = true)
        val html = """
        <head>
            <style>
                body {
                    font-family: 'Calibri', 'Arial', sans-serif;
                    margin-right: 25px;
                }
            </style>
            <script>
                function highlightSelectedText(color) {
                    var range, sel = window.getSelection();
                    if (sel.rangeCount && sel.getRangeAt) {
                        range = sel.getRangeAt(0);
                    };
                    document.designMode = "on";
                    if (range) {
                        sel.removeAllRanges();
                        sel.addRange(range);
                    };
                    if (!document.execCommand("HiliteColor", false, color)) {
                        document.execCommand("BackColor", false, color);
                    };
                    document.designMode = "off";
                }
                function makeItalic() {
                    var range, sel = window.getSelection();
                    if (sel.rangeCount && sel.getRangeAt) {
                        range = sel.getRangeAt(0);
                    };
                    document.designMode = "on";
                    if (range) {
                        sel.removeAllRanges();
                        sel.addRange(range);
                    };
                    document.execCommand("italic", false);
                    document.designMode = "off";
                }
                
                function boldSelectedText() {
                    var range, sel = window.getSelection();
                    if (sel.rangeCount && sel.getRangeAt) {
                        range = sel.getRangeAt(0);
                    };
                    document.designMode = "on";
                    if (range) {
                        sel.removeAllRanges();
                        sel.addRange(range);
                    };
                    document.execCommand("bold", false);
                    document.designMode = "off";
                }
                
                function underlineSelectedText() {
                    var range, sel = window.getSelection();
                    if (sel.rangeCount && sel.getRangeAt) {
                        range = sel.getRangeAt(0);
                    };
                    document.designMode = "on";
                    if (range) {
                        sel.removeAllRanges();
                        sel.addRange(range);
                    };
                    document.execCommand("underline", false);
                    document.designMode = "off";
                }
                
                function clearSelection() {
                    var sel = window.getSelection ? window.getSelection() : document.selection;
                    if (sel) {
                        if (sel.removeAllRanges) {
                            sel.removeAllRanges();
                        } else if (sel.empty) {
                            sel.empty();
                        }
                    }
                }
            </script>
        </head>
        <body>
            $cardBody
        </body>
        """.trimIndent()

        val window = MarkupCardWindow(cardrUI, html)
        val screenBounds = currentTab.cardWV.localToScreen(currentTab.cardWV.boundsInLocal)

        window.addOnCloseListener {
            if (!window.applyChanges)
                return@addOnCloseListener

            val innerBody = Jsoup.parse(it["cardBody"] as String).body()

            for (elem in innerBody.select("[style]")) {
                var style = elem.attr("style")
                if (style.contains("background-color")) {
                    val matchResult = Regex("background-color: ([a-zA-Z0-9()., ]+)").find(style)
                    if (matchResult != null) {
                        val color = matchResult.groups[1]!!.value
                        style += "mso-highlight: $color;"
                        elem.attr("style", style)
                    }
                }
            }

            currentTab.disableCardBodyEditOptions()
            currentTab.overrideBodyHTML = innerBody.html()

            currentTab.refreshHTML()

            if (!Prefs.get().hideFormattingDialog) {
                showInfoDialogBlocking("Applied highlighting & underlining changes.",
                    "While using Cardr, highlighting/underlining will be the last step in your card editing. After highlighting/underlining a card, you can no longer add or remove text to the card BODY (you can still change the header). If you wish to reset this, use the \"Restore to Original\" tool.",
                    "Never show this warning") {
                    Prefs.get().hidePastePlainTextDialog = true
                    cardrUI.menubarHelper.hidePlainPasteWarningMI.isSelected = true
                    Prefs.save()
                    showInfoDialogBlocking("Message will no longer be displayed.", "You can revert this setting under 'Settings > Messages > Hide highlight/underline dialog'.")
                }
            }

            if (Prefs.get().pastePlainText) {
                currentTab.statusBar.text = "Because of highlighting/underlining, plaintext paste will be overridden with HTML paste for this card."
            }
        }
        window.show()

        window.window.x = screenBounds.minX - 25
        window.window.y = screenBounds.minY - 150
        window.window.width = screenBounds.width + 225
        window.window.height = screenBounds.height + 150
    }

    fun copyCardToClipboard() {
        Toolkit.getDefaultToolkit()
            .systemClipboard
            .setContents(
                HTMLSelection(
                    currentTab.generateFullHTML(switchFont = true, forCopy = true, cardBodyReplacement = null)
                ),
                null
            )

        if (!Prefs.get().hideCopyDialog) {
            showInfoDialogBlocking("Copied card to clipboard.",
                "To paste this into a Word document or a Google Doc, use the default Ctrl/Cmd + V. Do NOT use 'Paste without Formatting' (F2 on Verbatim).",
                "Never show this message") {
                Prefs.get().hideCopyDialog = true
                cardrUI.menubarHelper.hideCopyPasteWarningMI.isSelected = true
                Prefs.save()
                showInfoDialogBlocking("Message will no longer be displayed.", "You can revert this setting under 'Settings > Messages > Hide copy/paste dialog'.")
            }
        }
    }

    private fun showSendToWordAlert() {
        if (Prefs.get().pastePlainText && !Prefs.get().hidePastePlainTextDialog) {
            showInfoDialogBlocking("Sent card to Verbatim.",
                "You currently have the PASTE PLAIN TEXT setting enabled, so you can currently ONLY send cards to Verbatim-enabled Word windows (NOT regular Word windows). If you would like to send cards to ALL word windows, go to 'Settings > Send to Word settings' and change the selected paste option to HTML.",
                "Never show this warning") {
                Prefs.get().hidePastePlainTextDialog = true
                cardrUI.menubarHelper.hidePlainPasteWarningMI.isSelected = true
                Prefs.save()
                showInfoDialogBlocking("Message will no longer be displayed.", "You can revert this setting under 'Settings > Messages > Hide plaintext paste dialog'.")
            }
        }
    }

    fun sendCardToWord() {
        if (wordWindowList.items.size == 0)
            refreshWordWindows()

        showSendToWordAlert()
        if (getOSType() == OS.WINDOWS){
            val msWord = WinMSWordInteractor()
            if (wordWindowList.items.size > 0) {
                msWord.selectWordWindowByDocName(wordWindowList.selectionModel.selectedItem)
            }
        } else if (getOSType() == OS.MAC){
            val msWord = MacMSWordInteractor()
            if (wordWindowList.items.size > 0) {
                msWord.selectWordWindowByDocName(wordWindowList.selectionModel.selectedItem)
            }
        }

        if (Prefs.get().pastePlainText && currentTab.overrideBodyHTML == null) {
            val cardBodyReplacement = "safd7asdyfkjahnw3k5nsd"
            val cardHtml = currentTab.generateFullHTML(switchFont = true, forCopy = true, cardBodyReplacement = cardBodyReplacement)
            val cardBodyIndex = cardHtml.indexOf(cardBodyReplacement)
            val beforeBody = cardHtml.substring(0, cardBodyIndex)
            var body = currentTab.generateCardBodyHTML(currentTab.reader!!.getBodyParagraphText(false), false)
            if (body.endsWith("\n"))
                body += "\n"
            val afterBody = cardHtml.substring(cardBodyIndex + cardBodyReplacement.length)

            pasteObject(beforeBody, KeyboardPasteMode.NORMAL)
            pasteObject(body, KeyboardPasteMode.PLAIN_TEXT)
            if (afterBody != "</span></p>\n </body>\n</html>")
                pasteObject(afterBody, KeyboardPasteMode.NORMAL)
        } else {
            pasteObject(currentTab.generateFullHTML(switchFont = true, forCopy = true, cardBodyReplacement = null), KeyboardPasteMode.NORMAL)
        }
    }

    fun removeSelectedText() {
        var success = false
        try {
            val selection = currentTab.cardWV.engine.executeScript("getSelectionTextCustom()") as String
            for (str in selection.split(Regex("[\\n\\t\\r]"))) {
                if (str.isNotBlank()) {
                    currentTab.removeWords.add(str)
                    success = true
                }
            }
            currentTab.refreshHTML()
        } catch (e: Exception) {
            success = false
        }
        if (!success) {
            val alert = Alert(Alert.AlertType.INFORMATION, "Please highlight text in the preview pane before clicking remove.")
            alert.dialogPane.stylesheets.add(CardrDesktop::class.java.getResource(Prefs.get().getStylesheet()).toExternalForm())
            alert.headerText = "No text selected"
            alert.showAndWait()
        }
    }
    
    fun onWindowResized(stage: Stage) {
        scrollPane.prefWidth = 230.0
        scrollPane.minWidth = 230.0
    }


    companion object {
        private val logger = LogManager.getLogger(ToolsPaneUI::class.java)
    }
}
