package me.sohamgovande.cardr.core.ui

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.effect.ColorAdjust
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.layout.*
import javafx.scene.text.Font
import javafx.scene.web.WebView
import javafx.stage.Stage
import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.core.auth.CardrUser
import me.sohamgovande.cardr.core.card.Author
import me.sohamgovande.cardr.core.card.AuthorNameFormat
import me.sohamgovande.cardr.core.card.AuthorListManager
import me.sohamgovande.cardr.core.card.Timestamp
import me.sohamgovande.cardr.core.ui.property.*
import me.sohamgovande.cardr.core.ui.windows.SignInLauncherOptions
import me.sohamgovande.cardr.core.ui.windows.SignInWindow
import me.sohamgovande.cardr.core.ui.windows.ocr.OCRCardBuilderWindow
import me.sohamgovande.cardr.core.web.WebsiteCardCutter
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.data.prefs.PrefsObject
import me.sohamgovande.cardr.data.updater.UpdateChecker
import me.sohamgovande.cardr.util.*
import org.apache.logging.log4j.LogManager
import org.jsoup.Jsoup
import java.awt.Desktop
import java.io.InputStream
import java.net.URL


class CardrUI(val stage: Stage) {

    private var authors: Array<Author> = arrayOf(Author(SimpleStringProperty(""), SimpleStringProperty("")))
    val changeListenerUpdateHTML = { _: ObservableValue<out String>, _: String, _: String ->
        Unit
        refreshHTML()
    }

    val urlTF = TextField()

    var propertyManager = CardPropertyManager(this)
    private lateinit var pGridScrollPane: ScrollPane
    var reader: WebsiteCardCutter? = null
    val cardBody: StringProperty = SimpleStringProperty("")

    var overrideBodyParagraphs: MutableList<String>? = null
    var overrideBodyHTML: String? = null

    val removeWords = arrayListOf<String>()
    val removeParagraphs = arrayListOf<String>()

    val cardWV = WebView()
    val statusBar = Label()

    private val panel = VBox()
    private val searchBarPanel = HBox()
    val gotoUrlBtn = Button("GO")
    private val bodyAreaPanel = HBox()
    private val cardDisplayArea = VBox()
    val menubarHelper = MenubarHelper(this, stage)
    var toolsUI = ToolsPaneUI(this)

    var ocrCardBuilderWindow: OCRCardBuilderWindow? = null

    var currentUser = CardrUser()

    var loaded = false

    init {
        currentUser.onSuccessfulLogin = menubarHelper::onSuccessfulLogin
    }

    fun initialize(): VBox {
        stage.widthProperty().addListener { _, _, _ -> onWindowResized() }
        stage.heightProperty().addListener { _, _, _ -> onWindowResized() }

        if (getOSType() == OS.WINDOWS) {
            logger.info("Generating default menu bar")
            menubarHelper.applyDefaultMenu(panel)
        }

        logger.info("Creating UI components")
        searchBarPanel.spacing = 5.0
        searchBarPanel.padding = Insets(5.0)

        urlTF.promptText = "Paste a URL to get started"
        urlTF.prefWidth = CardrDesktop.WIDTH - 50

        gotoUrlBtn.prefWidth = 50.0
        searchBarPanel.children.add(urlTF)
        searchBarPanel.children.add(gotoUrlBtn)

        bodyAreaPanel.padding = Insets(5.0)

        statusBar.font = Font.font(10.0)

        loadMenuIcons()

        val previewHeader = Label("Card Preview")
        previewHeader.font = Font.font(20.0)

        cardDisplayArea.children.add(previewHeader)
        cardDisplayArea.children.add(cardWV)
        cardDisplayArea.children.add(statusBar)

        val pGridVbox = VBox()
        val pGridHeader = Label("Properties")
        pGridHeader.font = Font.font(20.0)
        pGridVbox.children.addAll(pGridHeader, propertyManager.generatePropertyGrid())

        pGridScrollPane = ScrollPane(pGridVbox)
        pGridScrollPane.minWidth = 305.0
        pGridScrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        pGridScrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        pGridScrollPane.style = "-fx-background-color:transparent;"

        bodyAreaPanel.children.add(pGridScrollPane)
        bodyAreaPanel.children.add(cardDisplayArea)
        bodyAreaPanel.children.add(toolsUI.generateUI())

        panel.children.add(searchBarPanel)
        panel.children.add(bodyAreaPanel)

        propertyManager.bindAllToWebView()
        propertyManager.getByName<DateCardProperty>("Date")?.loadDateSeparatorLabels()

        return panel
    }

    fun loadMiniIcon(path: String, overrideDarkMode: Boolean, scale: Double): ImageView? {
        val copyResource: InputStream? = javaClass.getResourceAsStream(path)
        if (copyResource != null) {
            val image = Image(copyResource, 15.0 * scale, 15.0 * scale, true, true)
            val imageView = ImageView(image)
            if (Prefs.get().darkMode || overrideDarkMode) {
                val effect = ColorAdjust()
                effect.brightness = 1.0
                imageView.effect = effect
            }
            return imageView
        }
        return null
    }

    private fun visitURL(url: String) {
        Thread {
            currentUser.visitWebsite(url)
        }.start()
    }

    fun doDeferredLoad() {
        // Button actions
        gotoUrlBtn.setOnAction {
            Thread {
                try {
                    val reader = WebsiteCardCutter(this, urlTF.text, null)
                    this.reader = reader

                    overrideBodyHTML = null
                    enableCardBodyEditOptions()
                    removeWords.clear()
                    removeParagraphs.clear()
                    statusBar.text = ""

                    this.authors = reader.getAuthors() ?: this.authors
                    this.cardBody.set(reader.getBodyParagraphText(true))
                    overrideBodyParagraphs = null

                    Platform.runLater {
                        visitURL(urlTF.text)
                        propertyManager.loadFromReader(reader)
                    }
                } catch (e: Exception) {
                    val url = propertyManager.getByName<UrlCardProperty>("Url")?.getValue() ?: ""
                    showErrorDialog("Error reading page: ${e.message}", "A ${e.javaClass.simpleName} exception occurred while loading $url")
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

        Thread {
            logger.info("Checking login status")
            checkLoginStatus()

            logger.info("Checking for updates")
            checkForUpdates()
        }.start()
        loaded = true
    }

    fun loadMenuIcons() {
        toolsUI.restoreRemovedBtn.graphic = loadMiniIcon("/restore.png", false, 1.0)
        toolsUI.removeSelectedBtn.graphic = loadMiniIcon("/remove.png", false, 1.0)
        toolsUI.copyBtn.graphic = loadMiniIcon("/copy.png", false, 1.0)
        toolsUI.refreshBtn.graphic = loadMiniIcon("/refresh.png", false, 1.0)
        toolsUI.editCardFormatBtn.graphic = loadMiniIcon("/edit.png", false, 1.0)
        toolsUI.keepOnlySelectedBtn.graphic = loadMiniIcon("/keep-text.png", false, 1.0)
        toolsUI.markupBtn.graphic = loadMiniIcon("/markup.png", false, 1.0)
        toolsUI.ocrBtn.graphic = loadMiniIcon("/capture-ocr.png", false, 1.0)
        toolsUI.sendToWordBtn.graphic = loadMiniIcon("/word-grayscale.png", false, 1.0)

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

    private fun checkForUpdates() {
        UpdateChecker(this).checkForUpdates()
        logger.info("Initializing Word windows")
        toolsUI.refreshWordWindows()
    }

    private fun checkLoginStatus() {
        if ((CardrDesktop.IS_FIRST_LAUNCH && CardrDesktop.WAS_FIRST_LAUNCH_SUCCESSFUL) || CardrDesktop.OVERRIDE_LOGIN_CHECK)
            return
        if (Prefs.get().emailAddress.isEmpty()
            || Prefs.get().accessToken.isEmpty()) {
            // Needs to sign in
            logger.info("User needs to sign in - first time")
            Platform.runLater { SignInWindow(SignInLauncherOptions.WELCOME, currentUser).show() }
        } else {
            val renewResult = currentUser.renew()
            if (!renewResult.wasSuccessful()) {
                logger.info("User needs to sign in - token expired")
                // Access token has expired
                Platform.runLater { SignInWindow(SignInLauncherOptions.TOKEN_EXPIRED, currentUser).show() }
            } else {
                logger.info("Successfully renewed login token")
            }
        }
    }

    private fun generateDefaultHTML(): String {
        return """
            |<style>
            |    body { background-color: #${if (Prefs.get().darkMode) "373e43" else "f4f4f4"}; font-family: 'Calibri', 'Arial', sans-serif;}
            |</style>
            |<body> 
            |   Paste a URL above to get started!
            |</body>
            |""".trimMargin()
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

    fun onWindowResized() {
        urlTF.prefWidth = stage.width - 50
        cardWV.prefWidth = stage.width - 475
        cardWV.prefHeight = stage.height - 150
        propertyManager.onWindowResized(stage)
    }

    fun refreshHTML() {
        Platform.runLater {
            cardWV.engine?.loadContent(generateFullHTML(switchFont = false, forCopy = false, cardBodyReplacement = null))
        }
    }

    fun loadFromReader(reader: WebsiteCardCutter) {
        this.reader = reader
        reader.cardrUI = this

        Platform.runLater {
            this.urlTF.text = reader.getURL()
            visitURL(reader.getURL())
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

    fun disableCardBodyEditOptions() {
        toolsUI.keepOnlySelectedBtn.isDisable = true
        toolsUI.removeSelectedBtn.isDisable = true
        menubarHelper.keepSelectedMI.isDisable = true
        menubarHelper.removeSelectedMI.isDisable = true
    }

    fun enableCardBodyEditOptions() {
        toolsUI.keepOnlySelectedBtn.isDisable = false
        toolsUI.removeSelectedBtn.isDisable = false
        menubarHelper.keepSelectedMI.isDisable = false
        menubarHelper.removeSelectedMI.isDisable = false
    }

    fun updateWindowTitle(title: String) {
        Platform.runLater {
            var trimmed = title.substring(0, title.length.coerceAtMost(100))
            if (title.length >= 100)
                trimmed += "..."
            stage.title = "$trimmed - cardr ${CardrDesktop.CURRENT_VERSION}"
        }
    }

    companion object {
        private val logger = LogManager.getLogger(CardrUI::class.java)
    }
}
