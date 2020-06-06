package me.sohamgovande.cardr.core.ui.property

import javafx.beans.property.SimpleStringProperty
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import me.sohamgovande.cardr.core.card.Author
import me.sohamgovande.cardr.core.card.AuthorListManager
import me.sohamgovande.cardr.core.card.AuthorNameFormat
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.core.web.WebsiteCardCutter
import java.awt.Desktop
import java.net.URL

class AuthorsCardProperty(cardrUI: CardrUI) : CardProperty("Authors", arrayOf("{AuthorFullName}", "{AuthorLastName}", "{AuthorFirstName}", "{Qualifications}"), cardrUI) {

    val deleteAuthorButtons = arrayListOf<Button>()
    val searchButtons = arrayListOf<Button>()

    var value = arrayOf(Author("", ""))
    private var authorGrid = GridPane()

    override fun loadFromReader(reader: WebsiteCardCutter) {
        value = reader.getAuthors() ?: arrayOf(Author("", ""))
        generateAuthorsGrid()
    }

    override fun resolveMacro(macro: String): String {
        val manager = AuthorListManager(value)
        return when(macro) {
            "{AuthorFirstName}" -> manager.getAuthorName(AuthorNameFormat.FIRST_NAME)
            "{AuthorLastName}" -> manager.getAuthorName(AuthorNameFormat.LAST_NAME)
            "{AuthorFullName}" -> manager.getAuthorName(AuthorNameFormat.FULL_NAME)
            "{Qualifications}" -> manager.getAuthorQualifications()
            else -> ""
        }
    }

    private fun generateAuthorsGrid() {
        deleteAuthorButtons.clear()
        authorGrid.children.clear()
        searchButtons.clear()

        authorGrid.vgap = 2.0
        authorGrid.hgap = 2.0

        val addAuthor = Button("Add Author...")
        addAuthor.prefWidth = 225.0
        authorGrid.add(addAuthor, 0, 0, 3, 1)

        var uiRowIndex = 1

        for ((index, author) in value.withIndex()) {
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
            deleteAuthor.graphic = cardrUI.loadMiniIcon("/remove.png", false, 1.0)
            deleteAuthorButtons.add(deleteAuthor)
            deleteAuthor.prefWidth = 25.0
            if (value.size == 1)
                deleteAuthor.isDisable = true

            val authorGridQuals = TextField()
            authorGridQuals.promptText = "Qualifications"
            authorGridQuals.textProperty().bindBidirectional(author.qualifications)
            bindToRefreshWebView(authorGridQuals)

            val searchQuals = Button()
            searchQuals.graphic = cardrUI.loadMiniIcon("/search.png", false, 1.0)
            searchButtons.add(searchQuals)
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

            deleteAuthor.setOnAction {
                val authorsMutable = value.toMutableList()
                authorsMutable.removeAt(index)
                value = authorsMutable.toTypedArray()

                generateAuthorsGrid()
                cardrUI.refreshHTML()
            }

        }

        addAuthor.setOnAction {
            val authorsMutable = value.toMutableList()
            authorsMutable.add(Author(SimpleStringProperty(""), SimpleStringProperty("")))
            value = authorsMutable.toTypedArray()

            generateAuthorsGrid()
            cardrUI.refreshHTML()
        }
    }

    override fun generateEditUI(): Node {
        generateAuthorsGrid()
        return VBox(authorGrid)
    }

    override fun bindProperties() {
        // NA, implemented in generation code
    }
}