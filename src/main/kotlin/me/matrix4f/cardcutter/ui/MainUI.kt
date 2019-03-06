package me.matrix4f.cardcutter.ui

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
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
import me.matrix4f.cardcutter.util.pasteCardToVerbatim
import me.matrix4f.cardcutter.web.UrlDocReader
import org.jsoup.Jsoup
import java.awt.Desktop
import java.awt.Toolkit
import java.io.InputStream
import java.net.URL

class MainUI {

    var authors: Array<Author> = arrayOf(Author(SimpleStringProperty(""), SimpleStringProperty("")))
    var title: StringProperty = SimpleStringProperty("")
    var timestamp: Timestamp = Timestamp()
    var publisher: StringProperty = SimpleStringProperty("")
    var url: StringProperty = SimpleStringProperty("")
    var cardTag: StringProperty = SimpleStringProperty("TAG")
    val changeListenerUpdateHTML = { _: ObservableValue<out String>, _: String, _: String -> Unit
        // Sufficiently delay it to occur after the event goes through
        refreshHTML()
    }
    val cardBody: StringProperty = SimpleStringProperty("")

    val propertyUrlTextField = TextField()
    val propertyPubTextField = TextField()
    val propertyDateDayTextField = TextField()
    val propertyDateMonthTextField = TextField()
    val propertyDateYearTextField = TextField()
    val propertyTitleTextField = TextField()
    val cardTagTextField = TextField()
    val urlTextField = TextField()

    var cardDisplay: WebView? = null

    var lastUI: GridPane? = null
    val pGrid = GridPane()
    val generateAuthorGridBoxCallback: (GridPane) -> Unit = {
        pGrid.children.remove(lastUI)
        pGrid.requestLayout()
        pGrid.add(it, 1, 5)
        lastUI = it
    }
    var loaded = false

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
            authorsMutable.add(Author(SimpleStringProperty(""),SimpleStringProperty("")))
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
                |<div style="font-family: 'Calibri', 'Segoe UI';">
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

    fun initialize(): VBox {
        val panel = VBox()

        val searchBarPanel = HBox();
        searchBarPanel.spacing = 5.0
        searchBarPanel.padding = Insets(5.0)

        urlTextField.promptText = "Paste URL"
        urlTextField.prefWidth = CardCutterApplication.WIDTH - 50
        val gotoUrlButton = Button("GO")
        gotoUrlButton.prefWidth = 50.0
        searchBarPanel.children.add(urlTextField)
        searchBarPanel.children.add(gotoUrlButton)

        val bodyAreaPanel = HBox()
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

        val slashTextField = TextField("/")
        slashTextField.isEditable = false
        slashTextField.prefColumnCount = 1
        slashTextField.style = "-fx-background-color: #f4f4f4"
        val slashTextField2 = TextField("/")
        slashTextField2.isEditable = false
        slashTextField2.prefColumnCount = 1
        slashTextField2.style = "-fx-background-color: #f4f4f4"

        pGrid.add(Label("Date"), 0, 2)
        val dateHBox = HBox()
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
        generateAuthorGridBoxCallback(generateAuthorsGrid(generateAuthorGridBoxCallback))

        pGrid.columnConstraints.add(ColumnConstraints(60.0))
        pGrid.columnConstraints.add(ColumnConstraints(225.0))

        val msWordInteractor = MSWordInteractor()

        pGrid.add(Label("Verbatim"), 0, 6)

        val exportToWordSettings = VBox()
        exportToWordSettings.spacing = 5.0;

        val header = Label("Send Card to Verbatim")
        header.style = "-fx-font-weight: bold;"
        header.prefWidth = 225.0
        header.textAlignment = TextAlignment.CENTER
        exportToWordSettings.children.add(header)

        val exportToWordHBox = GridPane();
        exportToWordHBox.hgap = 5.0;


        exportToWordHBox.add(Label("Window:"), 0, 0)
        val wordWindowList = ComboBox<String>(FXCollections.observableList(msWordInteractor.getValidWordWindows()))
        if (!wordWindowList.items.isEmpty()) {
            wordWindowList.selectionModel.select(0)
        }
        wordWindowList.padding = Insets(0.0, 0.0, 0.0, 10.0);
        exportToWordHBox.add(wordWindowList, 1, 0)


        val refreshBtn = Button()

        val refreshResource: InputStream? = javaClass.getResourceAsStream("/refresh.png")
        if (refreshResource != null) {
            val refreshBtnImage = Image(refreshResource, 20.0, 20.0, true, true)
            refreshBtn.graphic = ImageView(refreshBtnImage)
        } else {
            refreshBtn.text = "Refresh"
        }

        exportToWordHBox.add(refreshBtn, 2, 0)
        exportToWordSettings.children.add(exportToWordHBox)

        val exportBtn = Button("Send to Verbatim")
        exportToWordSettings.children.add(exportBtn)
        pGrid.add(exportToWordSettings, 1, 6)

        val cardDisplayArea = VBox()

        val cardDisplayMenu = HBox()
        cardDisplayMenu.padding = Insets(0.0, 5.0, 5.0, 5.0)
        cardDisplayMenu.spacing = 5.0

        val copyBtn = Button("Copy to Clipboard")
        cardDisplayMenu.children.add(copyBtn)

        val cardDisplay = WebView()
        cardDisplay.engine.loadContent(generateDefaultHTML())
        this.cardDisplay = cardDisplay

        cardDisplayArea.children.add(cardDisplayMenu)
        cardDisplayArea.children.add(cardDisplay)

        bodyAreaPanel.children.add(pGrid)
        bodyAreaPanel.children.add(cardDisplayArea)

        panel.children.add(searchBarPanel)
        panel.children.add(bodyAreaPanel)

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

        copyBtn.setOnAction {
            Toolkit.getDefaultToolkit()
                .systemClipboard
                .setContents(
                    HtmlSelection(
                        Jsoup.parseBodyFragment(generateHTMLContent()).getElementById("copy").html()
                    ),
                    null
                )
        }

        refreshBtn.setOnAction {
            wordWindowList.items = FXCollections.observableList(msWordInteractor.getValidWordWindows())
            if (!wordWindowList.items.isEmpty()) {
                wordWindowList.selectionModel.select(0)
            }
        }

        exportBtn.setOnAction {
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

        loaded = true
        return panel
    }
}
