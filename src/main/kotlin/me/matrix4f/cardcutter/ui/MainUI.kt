package me.matrix4f.cardcutter.ui

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import javafx.scene.web.WebView
import me.matrix4f.cardcutter.CardCutterApplication
import me.matrix4f.cardcutter.card.Author
import me.matrix4f.cardcutter.card.Cite
import me.matrix4f.cardcutter.card.Timestamp
import me.matrix4f.cardcutter.platformspecific.MSWordInteractor
import me.matrix4f.cardcutter.prefs.Prefs
import me.matrix4f.cardcutter.prefs.windows.CitePrefsWindow
import me.matrix4f.cardcutter.prefs.windows.FontPrefsWindow
import me.matrix4f.cardcutter.util.pasteCardToVerbatim
import me.matrix4f.cardcutter.util.recordTime
import me.matrix4f.cardcutter.web.UrlDocReader
import org.jsoup.Jsoup
import java.awt.Desktop
import java.awt.Toolkit
import java.io.InputStream
import java.net.URL

class MainUI {

    private var authors: Array<Author> = arrayOf(Author(SimpleStringProperty(""), SimpleStringProperty("")))
    private var title: StringProperty = SimpleStringProperty("")
    private var timestamp: Timestamp = Timestamp()
    private var publisher: StringProperty = SimpleStringProperty("")
    private var url: StringProperty = SimpleStringProperty("")
    private var cardTag: StringProperty = SimpleStringProperty("TAG")
    private val changeListenerUpdateHTML = { _: ObservableValue<out String>, _: String, _: String ->
        Unit
        // Sufficiently delay it to occur after the event goes through
        refreshHTML()
    }
    private val cardBody: StringProperty = SimpleStringProperty("")

    private val propertyUrlTextField = TextField()
    private val propertyPubTextField = TextField()
    private val propertyDateDayTextField = TextField()
    private val propertyDateMonthTextField = TextField()
    private val propertyDateYearTextField = TextField()
    private val propertyTitleTextField = TextField()
    private val cardTagTextField = TextField()
    private val urlTextField = TextField()

    private val cardDisplay = WebView()

    private var lastUI: GridPane? = null
    private val pGrid = GridPane()
    private var generateAuthorGridBoxCallback: (GridPane) -> Unit = {}
    var loaded = false

    private val panel = VBox()
    private val searchBarPanel = HBox();
    private val gotoUrlButton = Button("GO")
    private val bodyAreaPanel = HBox()

    private val slashTextField = TextField("/")
    private val slashTextField2 = TextField("/")
    private val dateHBox = HBox()

    private val cardDisplayArea = VBox()
    private val cardDisplayMenu = HBox()

    private val exportToWordSettings = VBox()
    private val copyBtn = Button("Copy to Clipboard")
    private val exportBtn = Button("Send to Verbatim")

    private val refreshBtn = Button()

    private val wordWindowList = ComboBox<String>()

    private fun generateAuthorsGrid(regenerateUI: (GridPane) -> Unit): GridPane {
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

            val deleteAuthor = Button("X")
            deleteAuthor.prefWidth = 25.0

            val authorGridQuals = TextField()
            authorGridQuals.promptText = "Qualifications"
            authorGridQuals.textProperty().bindBidirectional(author.qualifications)
            bindToRefreshWebView(authorGridQuals)

            val searchQuals = Button("...")
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
                |body { background-color: #f4f4f4; }
            |</style>""".trimMargin()
    }

    private fun generateHTMLContent(): String {
        val cite = Cite(
            authors,
            timestamp,
            title.get(),
            publisher.get(),
            url.get()
        )
        return """
            |<style>
                |body { background-color: #f4f4f4; }
            |</style>
            |<div id="copy">
                |<div style="font-family: '${Prefs.get().fontName}', 'Arial';">
                    |<h4 style="font-size: '1.0833em';">${cardTag.get()}</h4>
                    |<span>${cite.toString(true)}</span>
                    |<p>${cardBody.get()}</p>
                |</div>
            |</div>""".trimMargin()
    }


    private fun refreshHTML() {
        Platform.runLater { cardDisplay?.engine?.loadContent(generateHTMLContent()) }
    }

    fun loadFromReader(reader: UrlDocReader) {
        Platform.runLater {
            this.urlTextField.text = reader.getURL()
            this.authors = reader.getAuthors() ?: this.authors
            this.timestamp = reader.getDate()
            this.publisher = SimpleStringProperty(reader.getPublication())
            this.url = SimpleStringProperty(reader.getURL())
            this.title = SimpleStringProperty(reader.getTitle() ?: "")
            this.cardTag.set(title.get())
            this.cardBody.set(reader.getBodyParagraphText(true))

            propertyTitleTextField.textProperty().bindBidirectional(this.title)
            propertyPubTextField.textProperty().bindBidirectional(this.publisher)
            propertyUrlTextField.textProperty().bindBidirectional(this.url)

            propertyDateDayTextField.textProperty().bindBidirectional(this.timestamp.day)
            propertyDateMonthTextField.textProperty().bindBidirectional(this.timestamp.month)
            propertyDateYearTextField.textProperty().bindBidirectional(this.timestamp.year)

            generateAuthorGridBoxCallback(generateAuthorsGrid(generateAuthorGridBoxCallback))
        }

    }

    private fun generateMenuBar(): MenuBar {
        val menuBar = MenuBar()

        val toolsMenu = Menu("Tools")
        val copyMenuItem = MenuItem("Copy to Clipboard")
        copyMenuItem.accelerator = KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN)
        copyMenuItem.setOnAction { copyCardToClipboard() }

        toolsMenu.items.add(SeparatorMenuItem())

        val refreshWindowsMenuItem  = MenuItem("Refresh Windows")
        refreshWindowsMenuItem.accelerator = KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN)
        refreshWindowsMenuItem.setOnAction { refreshWordWindows() }

        val sendMenuItem = MenuItem("Send to Verbatim")
        sendMenuItem.accelerator = KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN)
        sendMenuItem.setOnAction { sendCardToVerbatim() }

        toolsMenu.items.add(copyMenuItem)
        toolsMenu.items.add(refreshWindowsMenuItem)
        toolsMenu.items.add(sendMenuItem)

        val settingsMenu = Menu("Settings")

        val cardFormatMenuItem = MenuItem("Cite")
        cardFormatMenuItem.accelerator = KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN)
        cardFormatMenuItem.setOnAction { CitePrefsWindow().show() }

        val fontMenuItem = MenuItem("Font")
        fontMenuItem.accelerator = KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN)
        fontMenuItem.setOnAction { FontPrefsWindow().show() }

        settingsMenu.items.add(cardFormatMenuItem)
        settingsMenu.items.add(fontMenuItem)

        menuBar.menus.add(toolsMenu)
        menuBar.menus.add(settingsMenu)
        return menuBar
    }

    fun initialize(): VBox {

        panel.children.add(VBox(generateMenuBar()))

        searchBarPanel.spacing = 5.0
        searchBarPanel.padding = Insets(5.0)

        urlTextField.promptText = "Paste URL"
        urlTextField.prefWidth = CardCutterApplication.WIDTH - 50
        gotoUrlButton.prefWidth = 50.0
        searchBarPanel.children.add(urlTextField)
        searchBarPanel.children.add(gotoUrlButton)

        bodyAreaPanel.padding = Insets(5.0)

        pGrid.hgap = 10.0
        pGrid.vgap = 10.0
        pGrid.prefWidth = 300.0
        pGrid.prefHeight = CardCutterApplication.HEIGHT // Take up the rest remaining space

        bindToRefreshWebView(propertyUrlTextField)
        pGrid.add(Label("URL"), 0, 0)
        pGrid.add(propertyUrlTextField, 1, 0)

        bindToRefreshWebView(propertyPubTextField)
        pGrid.add(Label("Publication"), 0, 1)
        pGrid.add(propertyPubTextField, 1, 1)

        bindToRefreshWebView(propertyDateDayTextField)
        propertyDateDayTextField.prefColumnCount = 2
        propertyDateDayTextField.promptText = "31"

        bindToRefreshWebView(propertyDateMonthTextField)
        propertyDateMonthTextField.prefColumnCount = 2
        propertyDateMonthTextField.promptText = "01"

        bindToRefreshWebView(propertyDateYearTextField)
        propertyDateYearTextField.prefColumnCount = 4
        propertyDateYearTextField.promptText = "2019"

        slashTextField.isEditable = false
        slashTextField.prefColumnCount = 1
        slashTextField.style = "-fx-background-color: #f4f4f4"

        slashTextField2.isEditable = false
        slashTextField2.prefColumnCount = 1
        slashTextField2.style = "-fx-background-color: #f4f4f4"

        pGrid.add(Label("Date"), 0, 2)

        dateHBox.spacing = 10.0
        dateHBox.children.add(propertyDateMonthTextField)
        dateHBox.children.add(slashTextField)
        dateHBox.children.add(propertyDateDayTextField)
        dateHBox.children.add(slashTextField2)
        dateHBox.children.add(propertyDateYearTextField)

        pGrid.add(dateHBox, 1, 2)

        bindToRefreshWebView(propertyTitleTextField)
        pGrid.add(Label("Title"), 0, 3)
        pGrid.add(propertyTitleTextField, 1, 3)

        cardTagTextField.textProperty().bindBidirectional(cardTag)
        bindToRefreshWebView(cardTagTextField)
        pGrid.add(Label("Card Tag"), 0, 4)
        pGrid.add(cardTagTextField, 1, 4)
        pGrid.add(Label("Authors"), 0, 5)

        pGrid.columnConstraints.add(ColumnConstraints(60.0))
        pGrid.columnConstraints.add(ColumnConstraints(225.0))

        pGrid.add(Label("Verbatim"), 0, 6)

        exportToWordSettings.spacing = 5.0;

        val header = Label("Send Card to Verbatim")
        header.style = "-fx-font-weight: bold;"
        header.prefWidth = 225.0
        header.textAlignment = TextAlignment.CENTER
        exportToWordSettings.children.add(header)

        val exportToWordHBox = GridPane();
        exportToWordHBox.hgap = 5.0;


        exportToWordHBox.add(Label("Window:"), 0, 0)

        wordWindowList.padding = Insets(0.0, 0.0, 0.0, 10.0);
        exportToWordHBox.add(wordWindowList, 1, 0)



        exportToWordHBox.add(refreshBtn, 2, 0)
        exportToWordSettings.children.add(exportToWordHBox)

        exportToWordSettings.children.add(exportBtn)
        pGrid.add(exportToWordSettings, 1, 6)

        cardDisplayMenu.padding = Insets(0.0, 5.0, 5.0, 5.0)
        cardDisplayMenu.spacing = 5.0

        cardDisplayMenu.children.add(copyBtn)

        cardDisplayArea.children.add(cardDisplayMenu)
        cardDisplayArea.children.add(cardDisplay)

        bodyAreaPanel.children.add(pGrid)
        bodyAreaPanel.children.add(cardDisplayArea)

        panel.children.add(searchBarPanel)
        panel.children.add(bodyAreaPanel)

        recordTime("init main ui")
        return panel
    }

    fun doDeferredLoad() {
        // Button actions

        gotoUrlButton.setOnAction {
            Thread {
                val reader = UrlDocReader(urlTextField.text)
                this.authors = reader.getAuthors() ?: this.authors
                this.timestamp = reader.getDate()
                this.publisher = SimpleStringProperty(reader.getPublication())
                this.url = SimpleStringProperty(reader.getURL())
                this.title = SimpleStringProperty(reader.getTitle() ?: "")
                this.cardTag.set(title.get())
                this.cardBody.set(reader.getBodyParagraphText(true))

                Platform.runLater {
                    propertyTitleTextField.textProperty().bindBidirectional(this.title)
                    propertyPubTextField.textProperty().bindBidirectional(this.publisher)
                    propertyUrlTextField.textProperty().bindBidirectional(this.url)

                    propertyDateDayTextField.textProperty().bindBidirectional(this.timestamp.day)
                    propertyDateMonthTextField.textProperty().bindBidirectional(this.timestamp.month)
                    propertyDateYearTextField.textProperty().bindBidirectional(this.timestamp.year)

                    generateAuthorGridBoxCallback(generateAuthorsGrid(generateAuthorGridBoxCallback))
                }
            }.start()
        }

        copyBtn.setOnAction { copyCardToClipboard() }

        val msWordInteractor = MSWordInteractor()
        wordWindowList.items = FXCollections.observableList(msWordInteractor.getValidWordWindows())
        if (!wordWindowList.items.isEmpty()) {
            wordWindowList.selectionModel.select(0)
        }

        refreshBtn.setOnAction { refreshWordWindows() }
        exportBtn.setOnAction { sendCardToVerbatim() }

        // Load the refresh icon
        val refreshResource: InputStream? = javaClass.getResourceAsStream("/refresh.png")
        if (refreshResource != null) {
            val refreshBtnImage = Image(refreshResource, 20.0, 20.0, true, true)
            refreshBtn.graphic = ImageView(refreshBtnImage)
        } else {
            refreshBtn.text = "Refresh"
        }

        urlTextField.setOnKeyPressed {
            if (it.isControlDown && it.text.equals("v")) {
                Platform.runLater { gotoUrlButton.fire() }
            }
        }

        // Web view default content
        cardDisplay.engine.loadContent(generateDefaultHTML())

        // Generate author grid box callback
        generateAuthorGridBoxCallback = {
            pGrid.children.remove(lastUI)
            pGrid.requestLayout()
            pGrid.add(it, 1, 5)
            lastUI = it
        }
        generateAuthorGridBoxCallback(generateAuthorsGrid(generateAuthorGridBoxCallback))

        recordTime("finish deferred loading")
        loaded = true
    }

    private fun refreshWordWindows() {
        wordWindowList.items = FXCollections.observableList(MSWordInteractor().getValidWordWindows())
        if (!wordWindowList.items.isEmpty()) {
            wordWindowList.selectionModel.select(0)
        }
    }

    private fun copyCardToClipboard() {
        Toolkit.getDefaultToolkit()
            .systemClipboard
            .setContents(
                HtmlSelection(
                    Jsoup.parseBodyFragment(generateHTMLContent()).getElementById("copy").html()
                ),
                null
            )
    }

    private fun sendCardToVerbatim() {
        val msWord = MSWordInteractor()
        val cite = Cite(
            authors,
            timestamp,
            title.get(),
            publisher.get(),
            url.get()
        )
        if (wordWindowList.items.size > 0) {
            msWord.selectWordWindowByDocName(wordWindowList.selectionModel.selectedItem)
        }
        pasteCardToVerbatim(cardTag.get(), cite, cardBody.get())
    }

}