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
import me.sohamgovande.cardr.data.files.FSCardData
import me.sohamgovande.cardr.data.files.CardrFileSystem
import me.sohamgovande.cardr.data.files.FSFolder
import me.sohamgovande.cardr.data.files.FSObj
import me.sohamgovande.cardr.data.prefs.Prefs

class FileManagerTabUI(cardrUI: CardrUI) : TabUI("Organizer", cardrUI) {

    private val bodyAreaPanel = HBox()
    private lateinit var treeView: TreeView<FSObj>
    private val btnAddFolder = Button("", loadMiniIcon("/folder-new.png", false, 1.0))
    private val btnDeleteFolder = Button("", loadMiniIcon("/folder-delete.png", false, 1.0))
    private val treePanel = VBox()
    private val rootItem = FSTreeItem(FSFolder(CardrDesktop.CURRENT_VERSION_INT, "/", mutableListOf()))

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

    private fun addFoldersToTree(parent: FSTreeItem, folders: List<FSFolder>) {
        for (folder in folders) {
            val folderItem = FSTreeItem(folder)
            parent.children.add(folderItem)
            if (folder.cardUUIDs.isNotEmpty()) {
                for (card in folder.getCards())
                    folderItem.children.add(FSTreeItem(card))
            }
            val subfolders = folder.getChildren(true)
            if (subfolders.isNotEmpty())
                addFoldersToTree(folderItem, subfolders)
        }
    }

    private fun createNewFolder(newPath: String, parent: FSTreeItem?): FSTreeItem {
        val newFolder = FSFolder(CardrDesktop.CURRENT_VERSION_INT, newPath, mutableListOf())
        CardrFileSystem.folders.add(newFolder)
        CardrFileSystem.saveFolders()

        val newItem = FSTreeItem(newFolder)
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
            val selectedFolder = getSelectedFolder()
            if (selectedFolder != null) {
                val deleteCardsToo = ButtonType("Delete cards", ButtonBar.ButtonData.OK_DONE)
                val keepCards = ButtonType("Move to Uncategorized", ButtonBar.ButtonData.OK_DONE)
                val cancel = ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)

                val alert = Alert(Alert.AlertType.CONFIRMATION, "", deleteCardsToo, keepCards, cancel)
                alert.dialogPane.stylesheets.add(CardrDesktop::class.java.getResource(Prefs.get().getStylesheet()).toExternalForm())
                alert.title = "Deletion Confirmation"
                alert.headerText = "Do you want to delete the cards in '${selectedFolder.getName()}', or just move them to 'Uncategorized'?"
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
            val selectedFolder = getSelectedFolder()
            val isSelected = selectedFolder != null
            val contentText = if (!isSelected) "Enter a name for your folder" else "Enter a subfolder name (in ${selection.selectedItem.value})."
            val dialog = TextInputDialog("Untitled Folder")
            dialog.dialogPane.stylesheets.add(CardrDesktop::class.java.getResource(Prefs.get().getStylesheet()).toExternalForm())
            dialog.headerText = contentText

            val result = dialog.showAndWait()
            if (result.isPresent) {
                val path = if (!isSelected) result.get() else "${selectedFolder!!.path}/${result.get()}"
                createNewFolder(path, if (isSelected) selection.selectedItem as FSTreeItem? else null)
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
            if (it.oldValue !is FSFolder)
                return@setOnEditCommit
            val old = it.oldValue as FSFolder
            val new = it.newValue as FSFolder
            new.cardUUIDs = old.cardUUIDs
            new.path = old.getParentFolder() + "/" + new.getName()
            CardrFileSystem.folders.remove(old)
            CardrFileSystem.folders.add(new)
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
        val folder = getSelectedFolder()
        if (folder != null) {
            val selectedItem = selection.selectedItem
            deleteFolder(folder, deleteCards)
            selectedItem.parent.children.remove(selectedItem)
        }
    }
    
    private fun getSelectedFolder(): FSFolder? {
        val selection = treeView.selectionModel
        if (selection.isEmpty || selection.selectedItem.value !is FSFolder)
            return null
        val folder = selection.selectedItem.value as FSFolder
        if (folder.path == "/")
            return null
        return folder
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

class FSTreeItem(val value: FSObj) : TreeItem<FSObj>(value)

class FolderTreeCell: TextFieldTreeCell<FSObj>(FSStringConverter()) {
    override fun updateItem(item: FSObj?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty)
            return
        if (item is FSFolder) {
            val suffix = if (item.cardUUIDs.isNotEmpty()) "full" else "empty"
            graphic = TabUI.loadMiniIcon("/folder-$suffix.png", false, 1.0)
        } else if (item is FSCardData) {
            graphic = TabUI.loadMiniIcon("/tree-icon-card.png", false, 1.0)
        }
    }
}

class FSStringConverter : StringConverter<FSObj>() {
    override fun toString(obj: FSObj?): String {
        if (obj is FSFolder)
            return obj.getName()
        if (obj is FSCardData) {
            return obj.cardPropertiesJson.getAsJsonObject("Card Tag").getAsJsonPrimitive("value").asString
        }
        return "Null"
    }
    override fun fromString(string: String?): FSObj {
        return FSFolder(CardrDesktop.CURRENT_VERSION_INT, string ?: "null", mutableListOf())
    }
}