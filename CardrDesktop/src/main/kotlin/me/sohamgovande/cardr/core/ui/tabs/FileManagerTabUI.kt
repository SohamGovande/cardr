package me.sohamgovande.cardr.core.ui.tabs

import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTreeCell
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.util.Callback
import javafx.util.StringConverter
import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.data.files.CardrFileSystem
import me.sohamgovande.cardr.data.files.FSFolder
import me.sohamgovande.cardr.data.prefs.Prefs

class FileManagerTabUI(cardrUI: CardrUI) : TabUI("Organizer", cardrUI) {

    private val bodyAreaPanel = HBox()
    private lateinit var treeView: TreeView<FSFolder>
    private val btnAddFolder = Button("", loadMiniIcon("/folder-new.png", false, 1.0))
    private val btnDeleteFolder = Button("", loadMiniIcon("/folder-delete.png", false, 1.0))
    private val treePanel = VBox()
    private val rootItem = FolderTreeItem(FSFolder(CardrDesktop.CURRENT_VERSION_INT, "/", mutableListOf()))

    override fun generate() {
        internalTab.isClosable = false

        generateTreeView()

        bodyAreaPanel.children.add(treePanel)

        panel.children.add(bodyAreaPanel)
    }

    override fun loadIcons() {
        super.loadIcons()
        btnAddFolder.graphic = loadMiniIcon("/folder-new.png", false, 1.0)
        btnDeleteFolder.graphic = loadMiniIcon("/folder-delete.png", false, 1.0)
        treeView.refresh()
    }

    private fun addFoldersToTree(parent: FolderTreeItem, folders: List<FSFolder>) {
        for (folder in folders) {
            val folderItem = FolderTreeItem(folder)
            parent.children.add(folderItem)
            val subfolders = folder.getChildren(true)
            if (subfolders.isNotEmpty())
                addFoldersToTree(folderItem, subfolders)
        }
    }

    private fun createNewFolder(newPath: String, parent: FolderTreeItem?): FolderTreeItem {
        val newFolder = FSFolder(CardrDesktop.CURRENT_VERSION_INT, newPath, mutableListOf())
        CardrFileSystem.folders.add(newFolder)
        CardrFileSystem.saveFolders()

        val newItem = FolderTreeItem(newFolder)
        (parent ?: rootItem).children.add(newItem)
        return newItem
    }

    private fun generateTreeView() {
        treeView = TreeView(rootItem)
        treeView.minWidth = 300.0
        addFoldersToTree(rootItem, CardrFileSystem.getTopLevelFolders())

        btnAddFolder.tooltip = Tooltip("Create new folder")
        btnDeleteFolder.tooltip = Tooltip("Delete selected folder")

        btnDeleteFolder.setOnAction {
            val selection = treeView.selectionModel
            val isSelected = !(selection.isEmpty || selection.selectedItem.value.path == "/")

            if (isSelected) {
                val deleteCardsToo = ButtonType("Delete cards", ButtonBar.ButtonData.OK_DONE)
                val keepCards = ButtonType("Move to Uncategorized", ButtonBar.ButtonData.OK_DONE)
                val cancel = ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)

                val alert = Alert(Alert.AlertType.CONFIRMATION, "", deleteCardsToo, keepCards, cancel)
                alert.dialogPane.stylesheets.add(
                    CardrDesktop::class.java.getResource(Prefs.get().getStylesheet()).toExternalForm()
                )
                alert.title = "Deletion Confirmation"
                alert.headerText = "Do you want to delete the cards in '${selection.selectedItem.value.getName()}', or just move them to 'Uncategorized'?"
                alert.dialogPane.minHeight = Region.USE_PREF_SIZE
                val result = alert.showAndWait()
                if (result.isPresent && result.get() == keepCards)
                    deleteSelectedFolder(false)
                else
                    deleteSelectedFolder(true)
            }
        }
        btnAddFolder.setOnAction {
            val selection = treeView.selectionModel
            val isSelected = !(selection.isEmpty || selection.selectedItem.value.path == "/")
            val contentText = if (!isSelected) "Enter a name for your folder" else "Enter a subfolder name (saved in ${selection.selectedItem.value})."
            val dialog = TextInputDialog("Untitled Folder")
            dialog.headerText = contentText

            val result = dialog.showAndWait()
            if (result.isPresent) {
                val path = if (!isSelected) result.get() else "${selection.selectedItem.value.path}/${result.get()}"
                createNewFolder(path, if (isSelected) selection.selectedItem as FolderTreeItem? else null)
            }
        }

        treeView.isEditable = true
        treeView.cellFactory = Callback { FolderTreeCell() }

        val menu = ContextMenu()
        val renameItem = MenuItem("Rename")
        val deleteSubmenu = Menu("Delete")
        val deleteFolderKeepCardsItem = MenuItem("Delete folder but KEEP cards")
        val deleteFolderDeleteCardsItem = MenuItem("Delete folder AND cards")
        deleteSubmenu.items.add(deleteFolderDeleteCardsItem)
        deleteSubmenu.items.add(deleteFolderKeepCardsItem)

        menu.items.add(renameItem)
        menu.items.add(deleteSubmenu)

        renameItem.setOnAction { treeView.edit(treeView.selectionModel.selectedItem) }
        deleteFolderKeepCardsItem.setOnAction { deleteSelectedFolder(false) }
        deleteFolderDeleteCardsItem.setOnAction { deleteSelectedFolder(true) }

        treeView.setOnEditCommit {
            it.newValue.cardUUIDs = it.oldValue.cardUUIDs
            it.newValue.path = it.oldValue.getParentFolder() + "/" + it.newValue.getName()
            CardrFileSystem.folders.remove(it.oldValue)
            CardrFileSystem.folders.add(it.newValue)
            CardrFileSystem.saveFolders()
        }
        treeView.contextMenu = menu

        val optionsBox = HBox()
        optionsBox.children.addAll(btnAddFolder, btnDeleteFolder)
        optionsBox.padding = Insets(5.0)
        optionsBox.spacing = 5.0

        treePanel.children.addAll(optionsBox, treeView)
    }

    private fun deleteSelectedFolder(deleteCards: Boolean) {
        val selection = treeView.selectionModel
        val isSelected = !(selection.isEmpty || selection.selectedItem.value.path == "/")
        if (isSelected) {
            val selectedItem = selection.selectedItem
            deleteFolder(selectedItem.value, true)
            selectedItem.parent.children.remove(selectedItem)
        }
    }

    private fun deleteFolder(folder: FSFolder, deleteCards: Boolean) {
        for (subfolder in folder.getChildren(true))
            deleteFolder(subfolder, deleteCards)

        if (!deleteCards) {
            val uncategorizedFolder = CardrFileSystem.findFolder("Uncategorized")
            if (uncategorizedFolder != null) {
                uncategorizedFolder.cardUUIDs.addAll(folder.cardUUIDs)
            } else {
                createNewFolder("Uncategorized", rootItem)
                return
            }
        } else {
            for (card in folder.getCards())
                CardrFileSystem.deleteCard(card, false)
        }
        CardrFileSystem.folders.remove(folder)
        CardrFileSystem.saveFolders()
    }

    override fun onWindowResized() {
        val stage = cardrUI.stage
        treeView.prefHeight = stage.height - 100
    }

}

class FolderTreeItem(val value: FSFolder) : TreeItem<FSFolder>(value)

class FolderTreeCell: TextFieldTreeCell<FSFolder>(FolderStringConverter()) {
    override fun updateItem(item: FSFolder?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty)
            return
        val suffix = if (item.cardUUIDs.isNotEmpty()) "full" else "empty"
        graphic = TabUI.loadMiniIcon("/folder-$suffix.png", false, 1.0)
    }
}

class FolderStringConverter : StringConverter<FSFolder>() {
    override fun toString(obj: FSFolder?): String {
        return obj?.getName() ?: "Null"
    }
    override fun fromString(string: String?): FSFolder {
        return FSFolder(CardrDesktop.CURRENT_VERSION_INT, string ?: "null", mutableListOf())
    }
}