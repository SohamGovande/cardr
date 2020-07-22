package me.sohamgovande.cardr.core.ui.tabs

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.web.WebView
import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.core.card.Author
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.core.ui.HTMLGeneratorHelper
import me.sohamgovande.cardr.core.ui.property.AuthorsCardProperty
import me.sohamgovande.cardr.core.ui.property.CardPropertyManager
import me.sohamgovande.cardr.core.ui.property.DateCardProperty
import me.sohamgovande.cardr.core.ui.property.UrlCardProperty
import me.sohamgovande.cardr.core.ui.windows.EditPropertiesWindow
import me.sohamgovande.cardr.core.web.CardWebScraper
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.data.prefs.PrefsObject
import me.sohamgovande.cardr.util.OS
import me.sohamgovande.cardr.util.getOSType
import me.sohamgovande.cardr.util.showErrorDialogUnblocking
import org.apache.logging.log4j.LogManager
import org.jsoup.Jsoup

class EditCardTabUI(cardrUI: CardrUI) : TabUI("Card Editor", cardrUI) {
    private var authors: Array<Author> = arrayOf(Author(SimpleStringProperty(""), SimpleStringProperty("")))
    val changeListenerUpdateHTML = { _: ObservableValue<out String>, _: String, _: String ->
        Unit
        refreshHTML()
    }

    val urlTF = TextField()

    var propertyManager = CardPropertyManager(this)
    private lateinit var pGridScrollPane: ScrollPane
    private var pGridVbox = VBox()
    private val editPropertiesBtn = Button("Customize List...")
    var reader: CardWebScraper? = null
    val cardBody: StringProperty = SimpleStringProperty("")

    var overrideBodyParagraphs: MutableList<String>? = null
    var overrideBodyHTML: String? = null

    val removeWords = arrayListOf<String>()
    val removeParagraphs = arrayListOf<String>()

    val cardWV = WebView()
    val statusBar = Label()

    private val searchBarPanel = HBox()
    val gotoUrlBtn = Button("GO")
    private val bodyAreaPanel = HBox()
    private val cardDisplayArea = VBox()

    var toolsUI = ToolsPaneUI(this, cardrUI)

    override fun generate() {
        logger.info("Creating UI components for Edit Card tab")
        searchBarPanel.spacing = 5.0
        searchBarPanel.padding = Insets(5.0)

        urlTF.promptText = "Paste a URL to get started"
        urlTF.prefWidth = CardrDesktop.WIDTH - 50

        gotoUrlBtn.prefWidth = 50.0
        searchBarPanel.children.add(urlTF)
        searchBarPanel.children.add(gotoUrlBtn)

        bodyAreaPanel.padding = Insets(5.0)

        statusBar.font = Font.font(10.0)

        loadIcons()

        val previewHeader = Label("Card Preview")
        previewHeader.font = Font.font(20.0)

        cardDisplayArea.children.add(previewHeader)
        cardDisplayArea.children.add(cardWV)
        cardDisplayArea.children.add(statusBar)

        pGridScrollPane = ScrollPane(pGridVbox)
        pGridScrollPane.minWidth = 305.0
        pGridScrollPane.maxWidth = 305.0
        pGridScrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        pGridScrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        pGridScrollPane.style = "-fx-background-color:transparent;"

        bodyAreaPanel.children.add(pGridScrollPane)
        bodyAreaPanel.children.add(cardDisplayArea)
        bodyAreaPanel.children.add(toolsUI.generateUI())

        refreshPropertiesGrid()

        panel.children.add(searchBarPanel)
        panel.children.add(bodyAreaPanel)

        propertyManager.bindAllToWebView()
        propertyManager.getByName<DateCardProperty>("Date")?.loadDateSeparatorLabels()

        onCloseListeners.add {
            cardrUI.updateTabClosingPolicy()
        }
    }


    fun refreshPropertiesGrid() {
        pGridVbox.children.clear()
        val pGridHeader = Label("Properties Editor")
        pGridHeader.font = Font.font(20.0)
        editPropertiesBtn.setOnAction { EditPropertiesWindow(this).show() }

        val spacer = VBox()
        spacer.minHeight = 15.0

        pGridVbox.children.addAll(pGridHeader, propertyManager.generatePropertyGrid(), spacer, editPropertiesBtn)
    }

    override fun doDeferredLoad() {
        // Button actions
        gotoUrlBtn.setOnAction {
            Thread {
                try {
                    val reader = CardWebScraper(cardrUI, { this }, urlTF.text, null)
                    this.reader = reader

                    overrideBodyHTML = null
                    enableCardBodyEditOptions()
                    removeWords.clear()
                    removeParagraphs.clear()
                    statusBar.text = ""

                    this.authors = reader.getAuthors() ?: this.authors
                    this.cardBody.set(reader.getBodyParagraphText(true))
                    overrideBodyParagraphs = null

                    Platform.runLater { propertyManager.loadFromReader(reader) }
                } catch (e: Exception) {
                    val url = propertyManager.getByName<UrlCardProperty>("Url")?.getValue() ?: ""
                    showErrorDialogUnblocking("Error reading page: ${e.message}", "A ${e.javaClass.simpleName} exception occurred while loading $url")
                    logger.error("Error scraping page", e)
                }
            }.start()
        }

        urlTF.setOnKeyPressed {
            if (((it.isControlDown || it.isMetaDown) && it.text == "v") || it.code == KeyCode.ENTER) {
                Platform.runLater { gotoUrlBtn.fire() }
            }
        }

        // Web view default content
        cardWV.engine.loadContent(generateDefaultHTML())
        cardWV.prefWidth = CardrDesktop.WIDTH - 450
        cardWV.minWidth = 200.0
        cardWV.prefHeight = CardrDesktop.HEIGHT - 100
    }

    override fun loadIcons() {
        toolsUI.restoreRemovedBtn.graphic = loadMiniIcon("/restore.png", false, 1.0)
        toolsUI.removeSelectedBtn.graphic = loadMiniIcon("/remove.png", false, 1.0)
        toolsUI.copyBtn.graphic = loadMiniIcon("/copy.png", false, 1.0)
        toolsUI.refreshBtn.graphic = loadMiniIcon("/refresh.png", false, 1.0)
        toolsUI.editCardFormatBtn.graphic = loadMiniIcon("/edit.png", false, 1.0)
        toolsUI.keepOnlySelectedBtn.graphic = loadMiniIcon("/keep-text.png", false, 1.0)
        toolsUI.markupBtn.graphic = loadMiniIcon("/markup.png", false, 1.0)
        toolsUI.ocrBtn.graphic = loadMiniIcon("/capture-ocr.png", false, 1.0)
        toolsUI.sendToWordBtn.graphic = loadMiniIcon("/word-grayscale.png", false, 1.0)

        editPropertiesBtn.graphic = loadMiniIcon("/settings.png", false, 1.0)

        val authorsProperty = propertyManager.getByName<AuthorsCardProperty>("Authors")
        if (authorsProperty != null) {
            for (btn in authorsProperty.deleteAuthorButtons) {
                btn.graphic = loadMiniIcon("/remove.png", false, 1.0)
            }

            for (btn in authorsProperty.searchButtons) {
                btn.graphic = loadMiniIcon("/search.png", false, 1.0)
            }
        }
    }

    fun generateFullHTML(switchFont: Boolean, forCopy: Boolean, cardBodyReplacement: String?): String {
        val spacePlaceholder = "sas8d9f7aj523kj5h123jkhsaf"
        val doc = Jsoup.parse(Prefs.get().cardFormat.replace("&nbsp;",spacePlaceholder))

        val fontElements = doc.select("font")
        val helper = HTMLGeneratorHelper()

        for (font in fontElements)
            helper.applyFontSizeToFontElem(font)

        for (span in doc.select("span, b, i, u, li, ul, ol"))
            helper.applyFontSizeToSpanElem(span)

        for (elem in doc.allElements) {
            if (elem.children().size > 0 && elem.ownText().isEmpty())
                continue
            val cardBody = cardBodyReplacement ?: generateCardBodyHTML(cardBody.get(), true)
            var macros = elem.html().replace("{CardBody}", cardBody)
            macros = propertyManager.applyMacros(macros)
            elem.html(macros)
        }

        if (!forCopy) {
            doc.select("head")[0].html("""
            <style>
                body { font-family: 'Arial'; font-size: 11pt; margin-right: 20px;
                ${if (Prefs.get().darkMode && !forCopy) {
                "background-color: #373e43; color: #ffffff;"
            } else "background-color: #f4f4f4;"}
            </style> 
            <script>
                function getSelectionTextCustom() {
                    var text = "";
                    if (window.getSelection) {
                        text = window.getSelection().toString();
                    } else if (document.selection && document.selection.type != "Control") {
                        text = document.selection.createRange().text;
                    }
                    return text;
                }
            </script>
        """.trimIndent())
        }

        for (elem in doc.select("p")) {
            val oldStyle = elem.parent().attr("style")
            elem.attr("style", "$oldStyle${if (oldStyle.contains("font-size:11pt;")) { "line-height:20px;" } else { "" }}margin: 1px 0px 12px 0px; padding: 0px 0px 0px 0px;")
        }
        for (elem in doc.select("h4")) {
            elem.attr("style", "padding: 0px 0px 0px 0px; margin: 0px 0px 0px 0px;")
        }

        var docHtml = doc.html().replace(spacePlaceholder, "&nbsp;")
        if (switchFont && getOSType() == OS.MAC) {
            docHtml = docHtml.replace("font-family:'${PrefsObject.MAC_CALIBRI_FONT}';", "font-family:'Calibri';")
        }

        if (Prefs.get().showParagraphBreaks && forCopy)
            docHtml = docHtml.replace("¶ ", "")

        return docHtml
    }

    fun generateCardBodyHTML(cardBody: String, cardBodyIsHTML: Boolean): String {
        if (overrideBodyHTML != null)
            return overrideBodyHTML!!

        var out = cardBody

        val paragraphSuffix = if (Prefs.get().showParagraphBreaks) "¶ " else ""

        if (cardBodyIsHTML) {
            if (Prefs.get().condense) {
                out = "<p class='cardbody'>${cardBody.replace("<p>", "").replace("</p>", paragraphSuffix)}</p>"
            } else {
                out = cardBody.replace("<p>", "<p class='cardbody'>").replace("</p>", "$paragraphSuffix</p>")
            }
        }

        for (remove in removeWords) {
            out = out.replace(remove, "")
        }

        for (remove in removeParagraphs) {
            out = out.replace(remove, "")
        }

        if (Prefs.get().showParagraphBreaks && cardBodyIsHTML) {
            val paragraphBegin = if (Prefs.get().condense) "" else "<p class='cardbody'>"
            val paragraphEnd = if (Prefs.get().condense) "" else "</p>"
            while (out.contains("$paragraphBegin ¶ $paragraphEnd$paragraphBegin ¶ $paragraphEnd"))
                out = out.replace("$paragraphBegin ¶ $paragraphEnd$paragraphBegin ¶ $paragraphEnd", "$paragraphBegin ¶ $paragraphEnd")
            if (out.startsWith("<p class='cardbody'> ¶ "))
                out = "<p class='cardbody'>" + out.substring("<p class='cardbody'> ¶ ".length)
            if (out.endsWith("$paragraphBegin ¶ $paragraphEnd"))
                out = out.substring(0, out.length - "$paragraphBegin ¶ $paragraphEnd".length)
            if (out.endsWith("¶ </p>"))
                out = out.substring(0, out.length - "¶ </p>".length)
        }

        while (out.contains("  "))
            out = out.replace("  ", " ")
        while (out.contains("\n \n"))
            out = out.replace("\n \n", "\n")
        while (out.startsWith(" ") || out.startsWith("\n"))
            out = out.substring(1)
        while (out.startsWith(" \n") || out.startsWith("\n "))
            out = out.substring(2)
        while (out.endsWith(" "))
            out = out.substring(0, out.length - 1)
        while (out.endsWith(" \n") || out.endsWith("\n "))
            out = out.substring(0, out.length - 2)
        return out
    }

    private fun generateDefaultHTML(): String {
        return """
            |<style>
            |    body { background-color: #${if (Prefs.get().darkMode) "373e43" else "f4f4f4"}; font-family: 'Calibri','Arial', sans-serif; ${if (Prefs.get().darkMode) "color: #ffffff;" else ""}}
            |</style>
            |<body> 
            |   Paste a URL above to get started!
            |</body>
            |""".trimMargin()
    }

    override fun onWindowResized() {
        val stage = cardrUI.stage
        toolsUI.onWindowResized(stage)
        propertyManager.onWindowResized(stage)

        urlTF.prefWidth = stage.width - 50
        cardWV.prefWidth = stage.width - 475
        cardWV.prefHeight = stage.height - 100
    }

    fun refreshHTML() {
        Platform.runLater {
            cardWV.engine?.loadContent(generateFullHTML(switchFont = false, forCopy = false, cardBodyReplacement = null))
        }
    }

    fun loadFromReader(reader: CardWebScraper) {
        this.reader = reader
        reader.cardrUI = cardrUI

        Platform.runLater {
            this.urlTF.text = reader.getURL()
            this.authors = reader.getAuthors() ?: this.authors
            propertyManager.loadFromReader(reader)

            this.cardBody.set(reader.getBodyParagraphText(true))
            overrideBodyParagraphs = null
        }

    }

    @Throws(Exception::class)
    fun keepOnlyText(text: String) {
        removeParagraphs.clear()

        var selection = text
            .replace("\n\n", " ")
            .replace("¶ ", "").trim()

        while (selection.contains("  "))
            selection = selection.replace("  ", " ")

        val paragraphs = reader!!.getBodyParagraphsText()
        var firstIndex = -1
        var lastIndex = -1

        for (i in paragraphs.indices) {
            val paragraph = paragraphs[i]

            if (!selection.contains(paragraph)) {
                if (firstIndex != -1 && lastIndex == -1)
                    lastIndex = i - 1
            } else if (paragraph.isNotBlank()) {
                if (firstIndex == -1) {
                    firstIndex = i
                }
            }
        }

        if (firstIndex != -1 && lastIndex == -1)
            lastIndex = paragraphs.size - 1

        if (firstIndex == -1 && lastIndex == -1)
            throw ArrayIndexOutOfBoundsException("You must highlight at least one full paragraph in the webpage.")

        val placeholder = "asfda8sdfaweh25k3h21klsamnfi5"
        var selectionOutsides = selection
        for (i in firstIndex..lastIndex) {
            selectionOutsides = selectionOutsides.replace(paragraphs[i], placeholder)
        }

        while (selectionOutsides.contains("$placeholder$placeholder"))
            selectionOutsides = selectionOutsides.replace("$placeholder$placeholder", placeholder)

        val beforeAfterSelection = selectionOutsides.split(placeholder)
        if (firstIndex != 0) {
            paragraphs[firstIndex - 1] = paragraphs[firstIndex - 1].replace(beforeAfterSelection[0].trim(), "")
            removeParagraphs.add(paragraphs[firstIndex - 1])
        }

        if (lastIndex != paragraphs.size - 1) {
            paragraphs[lastIndex + 1] = paragraphs[lastIndex + 1].replace(beforeAfterSelection[beforeAfterSelection.size - 1].trim(), "")
            removeParagraphs.add(paragraphs[lastIndex + 1])
        }

        for (i in 0 until firstIndex) {
            removeParagraphs.add(paragraphs[i])
        }

        for (i in lastIndex + 1 until paragraphs.size) {
            removeParagraphs.add(paragraphs[i])
        }
        refreshHTML()
    }

    // todo: fix when changing tabs
    fun disableCardBodyEditOptions() {
        toolsUI.keepOnlySelectedBtn.isDisable = true
        toolsUI.removeSelectedBtn.isDisable = true
        cardrUI.menubarHelper.keepSelectedMI.isDisable = true
        cardrUI.menubarHelper.removeSelectedMI.isDisable = true
    }

    fun enableCardBodyEditOptions() {
        toolsUI.keepOnlySelectedBtn.isDisable = false
        toolsUI.removeSelectedBtn.isDisable = false
        cardrUI.menubarHelper.keepSelectedMI.isDisable = false
        cardrUI.menubarHelper.removeSelectedMI.isDisable = false
    }

    fun updateWindowTitle(title: String) {
        var trimmed: String
        if (title.isBlank()) {
            trimmed = "Card Editor"
        } else {
            trimmed = title.substring(0, title.length.coerceAtMost(20))
            if (title.length >= 20)
                trimmed += "..."
        }
        internalTab.text = trimmed
        cardrUI.updateWindowTitle(title)
    }

    companion object {
        val logger = LogManager.getLogger(EditCardTabUI::class.java)
    }
}