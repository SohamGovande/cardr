package me.matrix4f.cardcutter.ui

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.effect.ColorAdjust
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.*
import javafx.scene.text.TextAlignment
import javafx.scene.web.WebView
import javafx.stage.Stage
import me.matrix4f.cardcutter.CardifyDebate
import me.matrix4f.cardcutter.auth.CardifyUser
import me.matrix4f.cardcutter.card.Author
import me.matrix4f.cardcutter.card.AuthorNameFormat
import me.matrix4f.cardcutter.card.Cite
import me.matrix4f.cardcutter.card.Timestamp
import me.matrix4f.cardcutter.platformspecific.MSWordInteractor
import me.matrix4f.cardcutter.platformspecific.MacMSWordInteractor
import me.matrix4f.cardcutter.prefs.Prefs
import me.matrix4f.cardcutter.prefs.PrefsObject
import me.matrix4f.cardcutter.ui.windows.*
import me.matrix4f.cardcutter.util.*
import me.matrix4f.cardcutter.web.WebsiteCardCutter
import netscape.javascript.JSException
import org.apache.commons.exec.util.StringUtils
import org.apache.logging.log4j.LogManager
import org.jsoup.Jsoup
import java.awt.Desktop
import java.awt.Toolkit
import java.io.InputStream
import java.lang.NullPointerException
import java.net.URL
import java.util.function.Consumer

class CardCuttingUI(private val stage: Stage) {

    private var authors: Array<Author> = arrayOf(Author(SimpleStringProperty(""), SimpleStringProperty("")))
    private var title: StringProperty = SimpleStringProperty("")
    private var timestamp: Timestamp = Timestamp()
    private var publisher: StringProperty = SimpleStringProperty("")
    private var url: StringProperty = SimpleStringProperty("")
    private var cardTag: StringProperty = SimpleStringProperty("")
    private val changeListenerUpdateHTML = { _: ObservableValue<out String>, _: String, _: String ->
        Unit
        // Sufficiently delay it to occur after the event goes through
        refreshHTML()
    }
    private val cardBody: StringProperty = SimpleStringProperty("")

    private val propertyUrlTextField = TextField()
    private val propertyPubTextField = TextField()

    private val propertyDayTF = TextField()
    private val propertyMonthTF = TextField()
    private val propertyYearTF = TextField()

    private val propertyTitleTextField = TextField()
    private val cardTagTextField = TextField()
    private val urlTF = TextField()

    private val cardWV = WebView()

    private var lastUI: GridPane? = null
    private val pGrid = GridPane()
    private lateinit var pGridScrollPane: ScrollPane
    private var generateAuthorGridBoxCallback: (GridPane) -> Unit = {}
    var loaded = false

    private val panel = VBox()
    private val searchBarPanel = HBox()
    private val gotoUrlButton = Button("GO")
    private val bodyAreaPanel = HBox()

    private val slashLabel = Label("      /      ")
    private val slashLabel2 = Label("      /      ")
    private val dateGrid = GridPane()

    private val cardDisplayArea = VBox()
    private val cardDisplayMenu = VBox()

    private val exportToWordSettings = VBox()
    private val copyBtn = Button("Copy")
    private val removeSelectedBtn = Button("Remove Selected Text")
    private val restoreRemovedBtn = Button("Restore to Original")
    private val keepOnlySelectedBtn = Button("Remove Except for Selected Text")
    private val editCardFormat = Button("Edit Card Format")
    private val exportBtn = Button("Send to Word")

    private val refreshBtn = Button()

    private val wordWindowList = ComboBox<String>()
    private val removeWords = arrayListOf<String>()
    private val removeParagraphs = arrayListOf<String>()

    private val deleteAuthorButtons = arrayListOf<Button>()
    private val searchButtons = arrayListOf<Button>()

    var currentUser = CardifyUser()

    private var reader: WebsiteCardCutter? = null

    fun initialize(): VBox {
        stage.widthProperty().addListener { _, _, _ -> onWindowResized() }
        stage.heightProperty().addListener { _, _, _ -> onWindowResized() }

        logger.info("Generating menu bar")
        panel.children.add(VBox(generateMenuBar()))

        logger.info("Creating UI components")
        searchBarPanel.spacing = 5.0
        searchBarPanel.padding = Insets(5.0)

        urlTF.promptText = "Paste a URL to get started"
        urlTF.prefWidth = CardifyDebate.WIDTH - 50

        gotoUrlButton.prefWidth = 50.0
        searchBarPanel.children.add(urlTF)
        searchBarPanel.children.add(gotoUrlButton)

        bodyAreaPanel.padding = Insets(5.0)

        pGrid.hgap = 10.0
        pGrid.vgap = 10.0
        pGrid.minWidth = 300.0
        pGrid.prefHeight = CardifyDebate.HEIGHT - 100 // Take up the rest remaining space

        bindToRefreshWebView(propertyUrlTextField)
        pGrid.add(Label("URL"), 0, 0)
        pGrid.add(propertyUrlTextField, 1, 0)

        bindToRefreshWebView(propertyPubTextField)
        pGrid.add(Label("Publication"), 0, 1)
        pGrid.add(propertyPubTextField, 1, 1)

        bindToRefreshWebView(propertyDayTF)
        propertyDayTF.prefColumnCount = 2
        propertyDayTF.promptText = "31"

        bindToRefreshWebView(propertyMonthTF)
        propertyMonthTF.prefColumnCount = 2
        propertyMonthTF.promptText = "01"

        bindToRefreshWebView(propertyYearTF)
        propertyYearTF.prefColumnCount = 4
        propertyYearTF.promptText = currentDate().year.toString()

        pGrid.add(Label("Date"), 0, 2)

        dateGrid.padding = Insets(0.0)
        dateGrid.add(propertyMonthTF, 0, 0)
        dateGrid.add(slashLabel, 1, 0)
        dateGrid.add(propertyDayTF, 2, 0)
        dateGrid.add(slashLabel2, 3, 0)
        dateGrid.add(propertyYearTF, 4, 0)

        pGrid.add(dateGrid, 1, 2)

        bindToRefreshWebView(propertyTitleTextField)
        pGrid.add(Label("Title"), 0, 3)
        pGrid.add(propertyTitleTextField, 1, 3)

        cardTagTextField.promptText = ""
        cardTagTextField.textProperty().bindBidirectional(cardTag)
        cardTagTextField.textProperty().addListener { _, _, text ->
            cardTagTextField.text = text.replace("--", "–").replace("–-","—").replace("---", "—")
        }

        bindToRefreshWebView(cardTagTextField)
        pGrid.add(Label("Card Tag"), 0, 4)
        pGrid.add(cardTagTextField, 1, 4)
        pGrid.add(Label("Authors"), 0, 5)

        pGrid.columnConstraints.add(ColumnConstraints(60.0))
        pGrid.columnConstraints.add(ColumnConstraints(225.0))

        pGrid.add(Label("Word"), 0, 6)

        exportToWordSettings.spacing = 5.0

        val header = Label("Send Card to Word")
        header.style = "-fx-font-weight: bold;"
        header.prefWidth = 225.0
        header.textAlignment = TextAlignment.CENTER
        exportToWordSettings.children.add(header)
        exportToWordSettings.children.add(Label("Select Word window:"))

        val exportToWordHBox = GridPane()
        exportToWordHBox.hgap = 5.0

        wordWindowList.padding = Insets(0.0, 0.0, 0.0, 10.0)
        exportToWordHBox.add(refreshBtn, 0, 0)
        exportToWordHBox.add(wordWindowList, 1, 0)
        exportToWordSettings.children.add(exportToWordHBox)

        cardWV.prefWidth = CardifyDebate.WIDTH - 300
        cardWV.prefHeight = CardifyDebate.HEIGHT - 100

        exportToWordSettings.children.add(exportBtn)
        pGrid.add(exportToWordSettings, 1, 6)

        cardDisplayMenu.padding = Insets(0.0, 5.0, 5.0, 5.0)
        cardDisplayMenu.spacing = 5.0

        loadMenuIcons()

        val cdm1 = FlowPane()
        cdm1.hgap = 5.0
        cdm1.vgap = 5.0
        cdm1.children.add(removeSelectedBtn)
        cdm1.children.add(keepOnlySelectedBtn)
        cdm1.children.add(restoreRemovedBtn)
        cdm1.children.add(copyBtn)
        cdm1.children.add(editCardFormat)
        cardDisplayMenu.children.add(cdm1)

        cardDisplayArea.children.add(cardDisplayMenu)
        cardDisplayArea.children.add(cardWV)

        pGridScrollPane = ScrollPane(pGrid)
        pGridScrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        pGridScrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        pGridScrollPane.style = "-fx-background-color:transparent;"
        bodyAreaPanel.children.add(pGridScrollPane)
        bodyAreaPanel.children.add(cardDisplayArea)

        panel.children.add(searchBarPanel)
        panel.children.add(bodyAreaPanel)

        logger.info("Initializing Word windows")
        refreshWordWindows()
        return panel
    }

    private fun loadMiniIcon(path: String): ImageView? {
        val copyResource: InputStream? = javaClass.getResourceAsStream(path)
        if (copyResource != null) {
            val image = Image(copyResource, 15.0, 15.0, true, true)
            val imageView = ImageView(image)
            if (Prefs.get().darkMode) {
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
        gotoUrlButton.setOnAction {
            Thread {
                try {
                    val reader = WebsiteCardCutter(urlTF.text, null)
                    this.reader = reader
                    removeWords.clear()
                    removeParagraphs.clear()

                    this.authors = reader.getAuthors() ?: this.authors
                    this.timestamp = reader.getDate()
                    this.publisher = SimpleStringProperty(reader.getPublication())
                    this.url = SimpleStringProperty(reader.getURL())
                    this.title = SimpleStringProperty(reader.getTitle() ?: "")
                    updateWindowTitle(reader.getTitle() ?: "")
                    this.cardTag.set(title.get())
                    this.cardBody.set(reader.getBodyParagraphText(true))

                    Platform.runLater {
                        visitURL(urlTF.text)
                        propertyTitleTextField.textProperty().bindBidirectional(this.title)
                        propertyPubTextField.textProperty().bindBidirectional(this.publisher)
                        propertyUrlTextField.textProperty().bindBidirectional(this.url)

                        propertyDayTF.textProperty().bindBidirectional(this.timestamp.day)
                        propertyMonthTF.textProperty().bindBidirectional(this.timestamp.month)
                        propertyYearTF.textProperty().bindBidirectional(this.timestamp.year)

                        generateAuthorGridBoxCallback(generateAuthorsGrid(generateAuthorGridBoxCallback))
                    }
                } catch (e: Exception) {
                    showErrorDialog("Error reading page: ${e.message}", "A ${e.javaClass.simpleName} exception occurred while loading $url")
                    logger.error("Error scraping page", e)
                }
            }.start()
        }

        copyBtn.setOnAction { copyCardToClipboard() }
        removeSelectedBtn.setOnAction { removeSelectedText() }
        restoreRemovedBtn.setOnAction {
            removeWords.clear()
            removeParagraphs.clear()
            refreshHTML()
            val alert = Alert(Alert.AlertType.INFORMATION)
            alert.headerText = "Article content restored to original."
            alert.showAndWait()
        }

        if (getOSType() == OS.WINDOWS) {
            val msWordInteractor = MSWordInteractor()
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

        keepOnlySelectedBtn.setOnAction { keepOnlySelectedText() }

        editCardFormat.setOnAction { FormatPrefsWindow().show() }

        refreshBtn.setOnAction { refreshWordWindows() }

        exportBtn.setOnAction { sendCardToVerbatim() }

        urlTF.setOnKeyPressed {
            if (((it.isControlDown || it.isMetaDown) && it.text == "v") || it.code == KeyCode.ENTER) {
                Platform.runLater { gotoUrlButton.fire() }
            }
        }

        // Web view default content
        cardWV.engine.loadContent(generateDefaultHTML())

        // Generate author grid box callback
        generateAuthorGridBoxCallback = {
            pGrid.children.remove(lastUI)
            pGrid.requestLayout()
            pGrid.add(it, 1, 5)
            lastUI = it
        }
        generateAuthorGridBoxCallback(generateAuthorsGrid(generateAuthorGridBoxCallback))

        propertyTitleTextField.textProperty().bindBidirectional(this.title)
        propertyPubTextField.textProperty().bindBidirectional(this.publisher)
        propertyUrlTextField.textProperty().bindBidirectional(this.url)

        propertyDayTF.textProperty().bindBidirectional(this.timestamp.day)
        propertyMonthTF.textProperty().bindBidirectional(this.timestamp.month)
        propertyYearTF.textProperty().bindBidirectional(this.timestamp.year)

        Thread {
            logger.info("Checking login status")
            checkLoginStatus()
        }.start()
        loaded = true
    }

    private fun loadMenuIcons() {
        restoreRemovedBtn.graphic = loadMiniIcon("/restore.png")
        removeSelectedBtn.graphic = loadMiniIcon("/remove.png")
        copyBtn.graphic = loadMiniIcon("/copy.png")
        refreshBtn.graphic = loadMiniIcon("/refresh.png")
        editCardFormat.graphic = loadMiniIcon("/edit.png")
        keepOnlySelectedBtn.graphic = loadMiniIcon("/keep-text.png")

        for (btn in deleteAuthorButtons) {
            btn.graphic = loadMiniIcon("/remove.png")
        }

        for (btn in searchButtons) {
            btn.graphic = loadMiniIcon("/search.png")
        }

    }

    private fun checkLoginStatus() {
        if ((CardifyDebate.IS_FIRST_LAUNCH && CardifyDebate.WAS_FIRST_LAUNCH_SUCCESSFUL) || CardifyDebate.OVERRIDE_LOGIN_CHECK)
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
            }
        }
    }

    private fun generateAuthorsGrid(regenerateUI: (GridPane) -> Unit): GridPane {
        deleteAuthorButtons.clear()

        val authorGrid = GridPane()
        authorGrid.vgap = 2.0
        authorGrid.hgap = 2.0

        val addAuthor = Button("Add Author...")
        addAuthor.prefWidth = 225.0
        authorGrid.add(addAuthor, 0, 0, 3, 1)

        var index = 0
        var uiRowIndex = 1

        for (author in authors) {
            val authorGridFName = TextField()
            authorGridFName.prefWidth = 100.0
            authorGridFName.promptText = "First name"
            authorGridFName.textProperty().bindBidirectional(author.firstName)
            bindToRefreshWebView(authorGridFName)

            val authorGridLName = TextField()
            authorGridLName.promptText = "Last name"
            authorGridLName.prefWidth = 100.0
            authorGridLName.textProperty().bindBidirectional(author.lastName)
            bindToRefreshWebView(authorGridLName)

            val deleteAuthor = Button()
            deleteAuthor.graphic = loadMiniIcon("/remove.png")
            deleteAuthorButtons.add(deleteAuthor)
            deleteAuthor.prefWidth = 25.0

            val authorGridQuals = TextField()
            authorGridQuals.promptText = "Qualifications"
            authorGridQuals.textProperty().bindBidirectional(author.qualifications)
            bindToRefreshWebView(authorGridQuals)

            val searchQuals = Button()
            searchQuals.graphic = loadMiniIcon("/search.png")
            searchButtons.add(deleteAuthor)
            searchQuals.prefWidth = 25.0

            authorGrid.add(authorGridFName, 0, uiRowIndex)
            authorGrid.add(authorGridLName, 1, uiRowIndex)
            authorGrid.add(deleteAuthor, 2, uiRowIndex)
            uiRowIndex++
            authorGrid.add(authorGridQuals, 0, uiRowIndex, 2, 1)
            authorGrid.add(searchQuals, 2, uiRowIndex)
            uiRowIndex++


            searchQuals.setOnAction {
                val name = "${authorGridFName.text} ${authorGridLName.text}".trim().replace(" ", "%20")
                if (name.isNotEmpty())
                    Desktop.getDesktop().browse(URL("https://www.google.com/search?q=$name").toURI())
            }
            val finalIndex = index
            deleteAuthor.setOnAction {
                if (authors.size > 1) {
                    val authorsMutable = authors.toMutableList()
                    authorsMutable.removeAt(finalIndex)
                    authors = authorsMutable.toTypedArray()

                    regenerateUI(generateAuthorsGrid(regenerateUI))
                    refreshHTML()
                }
            }

            index++
        }

        addAuthor.setOnAction {
            val authorsMutable = authors.toMutableList()
            authorsMutable.add(Author(SimpleStringProperty(""), SimpleStringProperty("")))
            authors = authorsMutable.toTypedArray()

            regenerateUI(generateAuthorsGrid(regenerateUI))
            refreshHTML()
        }

        return authorGrid
    }

    private fun bindToRefreshWebView(component: TextField) {
        component.textProperty().addListener(changeListenerUpdateHTML)
    }

    private fun generateDefaultHTML(): String {
        return """
            |<style>
                |body { background-color: #${if (Prefs.get().darkMode) "373e43" else "f4f4f4"}; }
            |</style>""".trimMargin()
    }

    private fun generateFullHTML(switchFont: Boolean, forCopy: Boolean, cardBodyReplacement: String?): String {
        val cite = createCite()
        val spacePlaceholder = "sas8d9f7aj523kj5h123jkhsaf"
        val doc = Jsoup.parse(Prefs.get().cardFormat.replace("&nbsp;",spacePlaceholder))

        val now = currentDate()
        val fontElements = doc.select("font")
        val fontMap = mapOf(
            Pair("1","8"),
            Pair("2","10"),
            Pair("3","11"),
            Pair("4","13"),
            Pair("5","18"),
            Pair("6","24"),
            Pair("7","36")
        )

        for (elem in doc.select("span")) {
            if (elem.hasAttr("style") && !elem.attr("style").contains("font-size")) {
                elem.attr("style","${elem.attr("style")}font-size:${fontMap["3"]}pt;")
            }
        }

        for (font in fontElements) {
            var parent = font.parent()
            while (!parent.tagName().equals("p") && !parent.tagName().equals("b") && !parent.tagName().matches(Regex("h."))) {
                parent = parent.parent()
            }
            var style = ""
            if (font.hasAttr("face"))
                style += "font-family:'${font.attr("face")}';"
            if (!font.hasAttr("size"))
                 font.attr("size", "3") // 12pt font
            style += "font-size:${fontMap[font.attr("size")]}pt;"
            font.tagName("span")
            font.attr("style",style)
            font.removeAttr("face")
            font.removeAttr("size")
        }

        for (elem in doc.allElements) {
            if (elem.children().size > 0 && elem.ownText().length == 0)
                continue
            val cardBody = cardBodyReplacement ?: getCardBodyHTML(cardBody.get(), true)
            elem.html(
                elem.html()
                    .replace("{AuthorLastName}", cite.getAuthorName(AuthorNameFormat.LAST_NAME))
                    .replace("{DateShortened}", cite.date.toString(false))
                    .replace("{AuthorFullName}", cite.getAuthorName(AuthorNameFormat.FULL_NAME))
                    .replace("{AuthorFirstName}", cite.getAuthorName(AuthorNameFormat.FIRST_NAME))
                    .replace("{Qualifications}", cite.getAuthorQualifications())
                    .replace("{DateFull}", cite.date.toString(true))
                    .replace("{CurrentDate}", "${now.monthValue}-${now.dayOfMonth}-${now.year}")
                    .replace("{Publication}", cite.publication)
                    .replace("{Title}", cite.title)
                    .replace("{Url}", cite.url)
                    .replace("{Tag}", cardTag.value)
                    .replace("{CardBody}", cardBody)
            )
        }

        doc.select("head")[0].html("""
            <style>
                body { font-family: 'System'; font-size: 11pt;
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

        for (elem in doc.select("p")) {
            val oldStyle = elem.parent().attr("style")
            elem.attr("style", "$oldStyle${if (oldStyle.contains("font-size:11pt;")) { "line-height:20px;" } else { "" }}margin: 1px 0px 12px 0px; padding: 0px 0px 0px 0px;")
        }
        for (elem in doc.select("h4")) {
            elem.attr("style", "padding: 0px 0px 0px 0px; margin: 0px 0px 0px 0px;")
        }

        var docHtml = doc.html().replace(spacePlaceholder, "&nbsp;")
        if (switchFont && getOSType() == OS.MAC) {
            docHtml = docHtml.replace("font-family:'${PrefsObject.MAC_CALIBRI_FONT}';", "")
        }

        if (Prefs.get().showParagraphBreaks && forCopy)
            docHtml = docHtml.replace("¶ ", "")

        return docHtml
    }

    private fun getCardBodyHTML(cardBody: String, cardBodyIsHTML: Boolean): String {
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
        cardWV.prefWidth = stage.width - 325
        cardWV.prefHeight = stage.height - 150
        pGrid.prefHeight = stage.height - 150
    }

    private fun refreshHTML() {
        Platform.runLater {
            cardWV.engine?.loadContent(generateFullHTML(switchFont = false, forCopy = false, cardBodyReplacement = null))
        }
    }

    fun loadFromReader(reader: WebsiteCardCutter) {
        this.reader = reader

        Platform.runLater {
            this.urlTF.text = reader.getURL()
            this.authors = reader.getAuthors() ?: this.authors
            this.timestamp = reader.getDate()
            this.publisher = SimpleStringProperty(reader.getPublication())
            this.url = SimpleStringProperty(reader.getURL())
            this.title = SimpleStringProperty(reader.getTitle() ?: "")
            updateWindowTitle(reader.getTitle() ?: "")

            this.cardTag.set(title.get())
            this.cardBody.set(reader.getBodyParagraphText(true))

            propertyTitleTextField.textProperty().bindBidirectional(this.title)
            propertyPubTextField.textProperty().bindBidirectional(this.publisher)
            propertyUrlTextField.textProperty().bindBidirectional(this.url)

            propertyDayTF.textProperty().bindBidirectional(this.timestamp.day)
            propertyMonthTF.textProperty().bindBidirectional(this.timestamp.month)
            propertyYearTF.textProperty().bindBidirectional(this.timestamp.year)

            generateAuthorGridBoxCallback(generateAuthorsGrid(generateAuthorGridBoxCallback))
        }

    }

    private fun generateMenuBar(): MenuBar {
        val menuBar = MenuBar()

        val accountMenu = Menu("Account")

        val signUpMI = MenuItem("Sign up...")
        signUpMI.setOnAction {
            Desktop.getDesktop().browse(URL("http://cardifydebate.x10.bz/sign-up.html").toURI())
        }

        val signInMI = MenuItem("Sign in...")
        signInMI.setOnAction { SignInWindow(SignInLauncherOptions.MANUAL_SIGNIN, currentUser).show() }
        val historyMI = MenuItem("Card History")
        historyMI.accelerator = KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN)
        historyMI.setOnAction {
            HistoryWindow().show()
        }

        accountMenu.items.add(signUpMI)
        accountMenu.items.add(signInMI)
        accountMenu.items.add(SeparatorMenuItem())
        accountMenu.items.add(historyMI)

        val toolsMenu = Menu("Tools")
        val copyMI = MenuItem("Copy card")
        copyMI.accelerator = KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)
        copyMI.setOnAction { copyCardToClipboard() }


        val refreshWordMI  = MenuItem("Refresh Word windows")
        refreshWordMI.accelerator = KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN)
        refreshWordMI.setOnAction { refreshWordWindows() }

        val sendMI = MenuItem("Send to Word")
        sendMI.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)
        sendMI.setOnAction { sendCardToVerbatim() }

        val removeSelectedMI = MenuItem("Remove Selected Text")
        removeSelectedMI.accelerator = KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)
        removeSelectedMI.setOnAction { removeSelectedText() }

        val keepSelectedMI = MenuItem("Remove Except for Selected Text")
        keepSelectedMI.accelerator = KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN)
        keepSelectedMI.setOnAction { keepOnlySelectedText() }

        toolsMenu.items.add(copyMI)
        toolsMenu.items.add(refreshWordMI)
        toolsMenu.items.add(sendMI)
        toolsMenu.items.add(SeparatorMenuItem())
        toolsMenu.items.add(removeSelectedMI)
        toolsMenu.items.add(keepSelectedMI)

        val settingsMenu = Menu("Settings")

        val formatMI = MenuItem("Edit card and cite format...")
        formatMI.setOnAction {
            val window = FormatPrefsWindow()
            window.addOnCloseListener(Consumer {
                Platform.runLater { refreshHTML() }
            })
            window.show()
        }

        val condenseMI = CheckMenuItem("Condense paragraphs")
        condenseMI.isSelected = Prefs.get().condense
        condenseMI.setOnAction {
            Prefs.get().condense = condenseMI.isSelected
            Prefs.save()
            refreshHTML()
        }

        val useSmallDatesMI = CheckMenuItem("Use MM-DD for ${currentDate().year}")
        useSmallDatesMI.isSelected = !Prefs.get().onlyCardYear
        useSmallDatesMI.setOnAction {
            Prefs.get().onlyCardYear = !useSmallDatesMI.isSelected
            Prefs.save()
            refreshHTML()
        }

        val useEtAlMI = CheckMenuItem("Use 'et al.' for >1 author")
        useEtAlMI.isSelected = Prefs.get().useEtAl
        useEtAlMI.setOnAction {
            Prefs.get().useEtAl = useEtAlMI.isSelected
            Prefs.save()
            refreshHTML()
        }

        val capitalizeAuthorsMI = CheckMenuItem("Capitalize authors' names")
        capitalizeAuthorsMI.isSelected = Prefs.get().capitalizeAuthors
        capitalizeAuthorsMI.setOnAction {
            Prefs.get().capitalizeAuthors = capitalizeAuthorsMI.isSelected
            Prefs.save()
            refreshHTML()
        }

        val endQualsWithCommaMI = CheckMenuItem("Automatically append \", \" to last author qualification")
        endQualsWithCommaMI.isSelected = Prefs.get().endQualsWithComma
        endQualsWithCommaMI.setOnAction {
            Prefs.get().endQualsWithComma = endQualsWithCommaMI.isSelected
            Prefs.save()
            refreshHTML()
        }

        val darkModeMI = CheckMenuItem("Night mode")
        darkModeMI.isSelected = Prefs.get().darkMode
        darkModeMI.setOnAction {
            Prefs.get().darkMode = darkModeMI.isSelected
            Prefs.save()

            stage.scene.stylesheets.remove("/styles.css")
            stage.scene.stylesheets.remove("/styles-dark.css")
            stage.scene.stylesheets.add(javaClass.getResource(Prefs.get().getStylesheet()).toExternalForm())
            refreshHTML()
            loadMenuIcons()

            if (!Prefs.get().darkMode) {
                val alert = Alert(Alert.AlertType.INFORMATION)
                alert.title = "Please restart Cardify"
                alert.headerText = "Please restart Cardify for the changes to take effect."
                alert.contentText = "Upon restart, your theme changes will be applied."
                alert.showAndWait()
            }
        }

        val showParagraphBreaksMI = CheckMenuItem("Show paragraphs breaks")
        showParagraphBreaksMI.isSelected = Prefs.get().showParagraphBreaks
        showParagraphBreaksMI.setOnAction {
            Prefs.get().showParagraphBreaks = showParagraphBreaksMI.isSelected
            Prefs.save()
            refreshHTML()
        }

        val pastePlainTextMI = CheckMenuItem("Paste Plain Text to Word")
        pastePlainTextMI.isSelected = Prefs.get().pastePlainText
        pastePlainTextMI.setOnAction {
            Prefs.get().pastePlainText = pastePlainTextMI.isSelected
            Prefs.save()
            refreshHTML()
        }

        settingsMenu.items.add(formatMI)
        settingsMenu.items.add(condenseMI)
        settingsMenu.items.add(useSmallDatesMI)
        settingsMenu.items.add(useEtAlMI)
        settingsMenu.items.add(endQualsWithCommaMI)
        settingsMenu.items.add(capitalizeAuthorsMI)
        settingsMenu.items.add(SeparatorMenuItem())
        settingsMenu.items.add(darkModeMI)
        settingsMenu.items.add(showParagraphBreaksMI)
        settingsMenu.items.add(pastePlainTextMI)

        val aboutMenu = Menu("About")

        val creditsMI = MenuItem("Credits...")
        creditsMI.setOnAction { CreditsWindow().show() }
        val donateMI = MenuItem("Support development/donate...")
        donateMI.setOnAction { Desktop.getDesktop().browse(URL("http://cardifydebate.x10.bz/donate.html").toURI()) }

        val websiteMI = MenuItem("Visit website...")
        websiteMI.setOnAction { Desktop.getDesktop().browse(URL("http://cardifydebate.x10.bz").toURI()) }
        val chromeMI = MenuItem("Get Chrome Extension...")
        chromeMI.setOnAction { Desktop.getDesktop().browse(URL("https://chrome.google.com/webstore/detail/cardifydebate/ifdnjffggmmjiammdpklgldliaaempce").toURI()) }
        val githubMI = MenuItem("Contribute on GitHub...")
        githubMI.setOnAction { Desktop.getDesktop().browse(URL("https://www.github.com/SohamGovande/CardifyDebate").toURI()) }

        aboutMenu.items.add(creditsMI)
        aboutMenu.items.add(donateMI)
        aboutMenu.items.add(SeparatorMenuItem())
        aboutMenu.items.add(websiteMI)
        aboutMenu.items.add(chromeMI)
        aboutMenu.items.add(githubMI)

        menuBar.menus.add(accountMenu)
        menuBar.menus.add(toolsMenu)
        menuBar.menus.add(settingsMenu)
        menuBar.menus.add(aboutMenu)
        return menuBar
    }

    private fun removeSelectedText() {
        var success = false
        try {
            val selection = cardWV.engine.executeScript("getSelectionTextCustom()") as String
            for (str in selection.split(Regex("[\\n\\t\\r]"))) {
                if (str.isNotBlank()) {
                    removeWords.add(str)
                    success = true
                }
            }
            refreshHTML()
        } catch (e: JSException) {
            success = false
        }
        if (!success) {
            val alert = Alert(Alert.AlertType.INFORMATION, "Please highlight text in the preview pane before clicking remove.")
            alert.headerText = "No text selected"
            alert.showAndWait()
        }
    }

    private fun refreshWordWindows() {
        if (getOSType() == OS.WINDOWS){
            wordWindowList.items = FXCollections.observableList(MSWordInteractor().getValidWordWindows())
            if (!wordWindowList.items.isEmpty()) {
                wordWindowList.selectionModel.select(0)
            }
        } else if (getOSType() == OS.MAC){
            wordWindowList.items = FXCollections.observableList(MacMSWordInteractor().getValidWordWindows())
            if (!wordWindowList.items.isEmpty()) {
                wordWindowList.selectionModel.select(0)
            }
        }
    }

    private fun keepOnlySelectedText() {
        var success: Boolean
        try {
            if (reader == null) {
                throw NullPointerException("No reader found")
            }
            keepOnlyText(cardWV.engine.executeScript("getSelectionTextCustom()") as String)
            success = true
        } catch (e: JSException) {
            logger.error(e)
            e.printStackTrace()
            success = false
        } catch (e: NullPointerException) {
            logger.error(e)
            e.printStackTrace()
            success = false
        } catch (e: Exception) {
            logger.error(e)
            e.printStackTrace()
            success = false
        }

        if (!success) {
            val alert = Alert(Alert.AlertType.INFORMATION, "Please highlight at least one full paragraph in the preview pane in order to use this tool.")
            alert.headerText = "Not enough text selected"
            alert.showAndWait()
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

        val paragraphs = reader!!.getBodyParagraphs().map { it.text() }.toMutableList()
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

    private fun copyCardToClipboard() {
        Toolkit.getDefaultToolkit()
            .systemClipboard
            .setContents(
                HTMLSelection(
                    generateFullHTML(switchFont = true, forCopy = true, cardBodyReplacement = null)
                ),
                null
            )
    }

    private fun sendCardToVerbatim() {
        if (reader == null)
            return

        if (wordWindowList.items.size == 0)
            refreshWordWindows()

        if (getOSType() == OS.WINDOWS){
            val msWord = MSWordInteractor()
            if (wordWindowList.items.size > 0) {
                msWord.selectWordWindowByDocName(wordWindowList.selectionModel.selectedItem)
            }
        } else if (getOSType() == OS.MAC){
            val msWord = MacMSWordInteractor()
            if (wordWindowList.items.size > 0) {
                msWord.selectWordWindowByDocName(wordWindowList.selectionModel.selectedItem)
            }
        }

        val cardBodyReplacement = "safd7asdyfkjahnw3k5nsd"
        val cardHtml = generateFullHTML(switchFont = true, forCopy = true, cardBodyReplacement = cardBodyReplacement)
        val cardBodyIndex = cardHtml.indexOf(cardBodyReplacement)
        val beforeBody = cardHtml.substring(0, cardBodyIndex)
        var body = getCardBodyHTML(reader!!.getBodyParagraphText(false), false)
        val afterBody = cardHtml.substring(cardBodyIndex + cardBodyReplacement.length)

        pasteObject(beforeBody, KeyboardPasteMode.NORMAL)
        pasteObject(body, KeyboardPasteMode.PLAIN_TEXT)
        println(body)
        if (afterBody != "</span></p>\n </body>\n</html>")
            pasteObject(afterBody, KeyboardPasteMode.NORMAL)
    }

    private fun updateWindowTitle(title: String) {
        Platform.runLater {
            var trimmed = title.substring(0, Math.min(title.length, 100))
            if (title.length >= 100)
                trimmed += "..."
            stage.title = "$trimmed - Cardify Debate ${CardifyDebate.CURRENT_VERSION}"
        }
    }

    private fun createCite() = Cite(
        authors,
        timestamp,
        title.get(),
        publisher.get(),
        url.get()
    )

    companion object {
        private val logger = LogManager.getLogger(CardCuttingUI::class.java)
    }
}