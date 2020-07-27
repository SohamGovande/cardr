package me.sohamgovande.cardr.core.ui.tabs

import javafx.scene.control.Button
import javafx.scene.control.TextInputDialog
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.data.files.CardrFileSystem
import me.sohamgovande.cardr.data.files.FSFolder

class FileManagerTabUI(cardrUI: CardrUI) : TabUI("Organizer", cardrUI) {

    private val bodyAreaPanel = HBox()
    private lateinit var treeView: TreeView<String>
    private val btnAddFolder = Button("New Folder")
    private val treePanel = VBox()

    override fun generate() {
        internalTab.isClosable = false

        generateTreeView()

        bodyAreaPanel.children.add(treePanel)

        panel.children.add(bodyAreaPanel)
    }

    private fun generateTreeView() {
        val rootItem = TreeItem("Saved Cards")
        for (folder in CardrFileSystem.folders) {
            val folderItem = TreeItem(folder.path)
            rootItem.children.add(folderItem)
        }
        treeView = TreeView(rootItem)
        treeView.minWidth = 300.0

        btnAddFolder.setOnAction {
            val dialog = TextInputDialog("New Folder")
            dialog.headerText = "Enter a name for your folder"
            val result = dialog.showAndWait()
            if (result.isPresent) {
                val path = result.get()
                val newFolder = FSFolder(CardrDesktop.CURRENT_VERSION_INT, path, mutableListOf())
                CardrFileSystem.folders.add(newFolder)
                CardrFileSystem.saveFolders()
                rootItem.children.add(TreeItem(path))
            }
        }

        treePanel.children.addAll(btnAddFolder, treeView)
    }

    override fun onWindowResized() {
        val stage = cardrUI.stage
        treeView.prefHeight = stage.height - 100
    }

}