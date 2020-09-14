package me.sohamgovande.cardr.core.ui.tabs

import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTreeCell
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.util.Callback
import javafx.util.StringConverter
import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.data.files.CardrFileSystem
import me.sohamgovande.cardr.data.files.FSFolder

class FileManagerTabUI(cardrUI: CardrUI) : TabUI("Organizer", cardrUI) {

    private val bodyAreaPanel = HBox()
    private lateinit var treeView: TreeView<FSFolder>
    private val btnAddFolder = Button("New Folder")
    private val treePanel = VBox()
    private val rootItem = FolderTreeItem(FSFolder(CardrDesktop.CURRENT_VERSION_INT, "/", mutableListOf()))

    override fun generate() {
        internalTab.isClosable = false

        generateTreeView()

        bodyAreaPanel.children.add(treePanel)

        panel.children.add(bodyAreaPanel)
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

    private fun createNewFolder(newPath: String, parent: FolderTreeItem?) {
        val newFolder = FSFolder(CardrDesktop.CURRENT_VERSION_INT, newPath, mutableListOf())
        CardrFileSystem.folders.add(newFolder)
        CardrFileSystem.saveFolders()

        val newItem = FolderTreeItem(newFolder)
        (parent ?: rootItem).children.add(newItem)
    }

    private fun generateTreeView() {
        treeView = TreeView(rootItem)
        treeView.minWidth = 300.0
        addFoldersToTree(rootItem, CardrFileSystem.getTopLevelFolders())

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
        treeView.cellFactory = Callback {
            TextFieldTreeCell<FSFolder>(object : StringConverter<FSFolder>() {
                override fun toString(obj: FSFolder?): String {
                    return obj?.getName() ?: "Null"
                }
                override fun fromString(string: String?): FSFolder {
                    return FSFolder(CardrDesktop.CURRENT_VERSION_INT, string ?: "null", mutableListOf())
                }
            })
        }

        val menu = ContextMenu()
        val renameItem = MenuItem("Rename")
        menu.items.add(renameItem)

        renameItem.setOnAction { treeView.edit(treeView.selectionModel.selectedItem) }

        treeView.setOnEditCommit {
            it.newValue.cardUUIDs = it.oldValue.cardUUIDs
            it.newValue.path = it.oldValue.getParentFolder() + "/" + it.newValue.getName()
            CardrFileSystem.folders.remove(it.oldValue)
            CardrFileSystem.folders.add(it.newValue)
            CardrFileSystem.saveFolders()
        }
        treeView.contextMenu = menu

        treePanel.children.addAll(btnAddFolder, treeView)
    }

    override fun onWindowResized() {
        val stage = cardrUI.stage
        treeView.prefHeight = stage.height - 100
    }

}

class FolderTreeItem(val value: FSFolder) : TreeItem<FSFolder>(value)