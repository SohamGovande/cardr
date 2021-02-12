package me.sohamgovande.cardr.core.ui.windows.markup

import javafx.collections.FXCollections
import javafx.concurrent.Worker
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.input.KeyEvent
import javafx.scene.layout.FlowPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.web.WebView
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.core.ui.tabs.TabUI
import me.sohamgovande.cardr.core.ui.windows.ModalWindow
import me.sohamgovande.cardr.data.prefs.Prefs
import netscape.javascript.JSObject
import java.awt.event.KeyEvent.VK_F1

class MarkupCardWindow(private val cardrUI: CardrUI, private val cardBodyHTML: String): ModalWindow("Highlight & Underline Card") {

    private val optionsPane = FlowPane()

    private val resetAllBtn = Button("Reset All")
    private val autoOperation = ComboBox<String>(FXCollections.observableArrayList("${AUTO_OPERATION_PREFIX}None", "${AUTO_OPERATION_PREFIX}Highlight", "${AUTO_OPERATION_PREFIX}Emphasize", "${AUTO_OPERATION_PREFIX}Bold", "${AUTO_OPERATION_PREFIX}Underline", "${AUTO_OPERATION_PREFIX}Unhighlight"))
    private val boldBtn = Button()
    private val underlineBtn = Button()
    private val emphasizeBtn = Button()
    private val highlightBtn = Button()
    private val unhighlightBtn = Button()
    private val settingsBtn = Button("Settings")

    private val cardWV = WebView()

    private val applyChangesBtn = Button("Apply Changes")
    private val discardChangesBtn = Button("Discard & Exit")
    var applyChanges = false

    override fun generateUI(): Scene {
        val vbox = VBox()
        vbox.padding = Insets(10.0)
        vbox.spacing = 10.0

        val header = Label("Highlight & Underline Card")
        header.font = Font.font(20.0)

        optionsPane.hgap = 5.0
        optionsPane.vgap = 5.0

        optionsPane.children.addAll(resetAllBtn, autoOperation, boldBtn, underlineBtn, emphasizeBtn, highlightBtn, unhighlightBtn)

        discardChangesBtn.setOnAction {
            close(null)
        }

        applyChangesBtn.setOnAction {
            applyChanges = true
            onCloseData["cardBody"] = cardWV.engine.executeScript("document.documentElement.outerHTML") as String
            window.onCloseRequest.handle(null)
        }

        val bottomOptions = HBox()
        bottomOptions.spacing = 10.0
        bottomOptions.children.addAll(
            settingsBtn,
            discardChangesBtn,
            applyChangesBtn
        )

        cardWV.setOnKeyPressed(this::onKeyPressed)
        cardWV.prefWidth = WIDTH - 50
        cardWV.prefHeight = 1000.0
        cardWV.engine.loadWorker.stateProperty().addListener { _, _, state ->
            if (state != Worker.State.SUCCEEDED)
                return@addListener
            val autoOperationValue = autoOperation.items[Prefs.get().autoOperation].replace(AUTO_OPERATION_PREFIX, "")
            cardWV.engine.executeScript("setAutoOperation('$autoOperationValue')")
            cardWV.engine.executeScript("setHighlightColor('${Prefs.get().highlightColor}')")
            (cardWV.engine.executeScript("window") as JSObject).setMember("javaConnector", MarkupJavaConnector(this))
        }
        cardWV.engine.loadContent(cardBodyHTML)

        autoOperation.selectionModel.select(Prefs.get().autoOperation)
        autoOperation.selectionModel.selectedIndexProperty().addListener { _, _, index ->
            val value = autoOperation.items[index as Int].replace(AUTO_OPERATION_PREFIX, "")
            cardWV.engine.executeScript("setAutoOperation(\"$value\")")
            Prefs.get().autoOperation = index
            Prefs.save()
        }
        resetAllBtn.setOnAction { cardWV.engine.loadContent(cardBodyHTML) }
        boldBtn.setOnAction { boldSelectedText(); clearSelection() }
        underlineBtn.setOnAction { underlineSelectedText(); clearSelection() }
        highlightBtn.setOnAction { highlightSelectedText(); clearSelection() }
        unhighlightBtn.setOnAction { unhighlightSelectedText(); clearSelection() }
        emphasizeBtn.setOnAction { emphasize() }
        settingsBtn.setOnAction {
            val settings = MarkupCardSettingsWindow()
            settings.addOnCloseListener { refreshShortcutTips() }
            settings.show()
        }

        refreshShortcutTips()

        vbox.children.add(header)
        vbox.children.add(optionsPane)
        vbox.children.add(cardWV)
        vbox.children.add(bottomOptions)
        loadMenuIcons()

        val scene = Scene(vbox, WIDTH, HEIGHT)
        scene.stylesheets.add(javaClass.getResource(Prefs.get().getStylesheet()).toExternalForm())
        super.window.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))
        return scene
    }

    fun emphasize() {
        cardWV.engine.executeScript("makeItalic()")
        val scrollPosX = cardWV.engine.executeScript("window.scrollX") as Int
        val scrollPosY = cardWV.engine.executeScript("window.scrollY") as Int
        var newHTML = cardWV.engine.executeScript("document.documentElement.outerHTML") as String

        val sections = MarkupSection.findSectionsInSelection(newHTML)

        for (section in sections) {
            section.isBold = section.hasHTMLAttribute(newHTML, "b", null)
            section.isUnderlined = section.hasHTMLAttribute(newHTML, "u", null)
        }

        val unemphasize = sections.all { it.isBold!! && it.isUnderlined!! }
        val oneShotEmphasize = sections.all { !it.isBold!! && !it.isUnderlined!! }
        if (unemphasize || oneShotEmphasize) {
            cardWV.engine.executeScript("makeItalic()")

            boldSelectedText()
            underlineSelectedText()
            clearSelection()
            return

        } else {
            for (section in sections) {
                if (section.isBold!! && !section.isUnderlined!!) {
                    newHTML = section.addTags(newHTML, "<u>", "</u>", sections)
                } else if (!section.isBold!! && section.isUnderlined!!) {
                    newHTML = section.addTags(newHTML, "<b>", "</b>", sections)
                } else if (!section.isBold!! && !section.isUnderlined!!) {
                    newHTML = section.addTags(newHTML, "<u><b>", "</u></b>", sections)
                }
            }
        }

        newHTML = newHTML.replace("<i>","").replace("</i>","")

        var hasLoaded = false
        cardWV.engine.loadContent(newHTML)
        cardWV.engine.loadWorker.stateProperty().addListener { _, _, value ->
            if (value != Worker.State.SUCCEEDED || hasLoaded)
                return@addListener
            hasLoaded = true

            cardWV.engine.executeScript("window.scrollBy($scrollPosX, $scrollPosY)")
        }
    }

    private fun refreshShortcutTips() {
        boldBtn.text = "Bold" + getShortcutName(Prefs.get().boldShortcut)
        underlineBtn.text = "Underline" + getShortcutName(Prefs.get().underlineShortcut)
        emphasizeBtn.text = "Emphasize" + getShortcutName(Prefs.get().emphasizeShortcut)
        highlightBtn.text = "Highlight" + getShortcutName(Prefs.get().highlightShortcut)
        unhighlightBtn.text = "Unhighlight" + getShortcutName(Prefs.get().unhighlightShortcut)
    }

    private fun getShortcutName(code: Int): String {
        return " (" + MarkupCardSettingsWindow.FUNCTION_KEYS[code - VK_F1] + ")"
    }

    private fun onKeyPressed(event: KeyEvent) {
        when(event.code.code) {
            Prefs.get().boldShortcut -> boldSelectedText()
            Prefs.get().underlineShortcut -> underlineSelectedText()
            Prefs.get().emphasizeShortcut -> emphasize()
            Prefs.get().highlightShortcut -> highlightSelectedText()
            Prefs.get().unhighlightShortcut -> unhighlightSelectedText()
        }
        clearSelection()
    }

    private fun highlightSelectedText() {
        cardWV.engine.executeScript("highlightSelectedText('${Prefs.get().highlightColor}')")
    }

    private fun unhighlightSelectedText() {
        cardWV.engine.executeScript("highlightSelectedText('#00000000')")
    }

    private fun boldSelectedText() {
        cardWV.engine.executeScript("boldSelectedText()")
    }

    private fun underlineSelectedText() {
        cardWV.engine.executeScript("underlineSelectedText()")
    }

    private fun clearSelection() {
        cardWV.engine.executeScript("clearSelection()")
    }

    fun loadMenuIcons() {
        highlightBtn.graphic = TabUI.loadMiniIcon("/highlight.png", false, 1.0)
        emphasizeBtn.graphic = TabUI.loadMiniIcon("/emphasize.png", false, 1.0)
        underlineBtn.graphic = TabUI.loadMiniIcon("/underline.png", false, 1.0)
        boldBtn.graphic = TabUI.loadMiniIcon("/bold.png", false, 1.0)
        unhighlightBtn.graphic = TabUI.loadMiniIcon("/eraser.png", false, 1.0)
        resetAllBtn.graphic = TabUI.loadMiniIcon("/reset-all.png", false, 1.0)
        settingsBtn.graphic = TabUI.loadMiniIcon("/settings.png", false, 1.0)
        applyChangesBtn.graphic = TabUI.loadMiniIcon("/checkmark.png", false, 1.0)
        discardChangesBtn.graphic = TabUI.loadMiniIcon("/close.png", false, 1.0)
    }

    companion object {
        const val WIDTH = 800.0
        const val HEIGHT = 400.0
        const val AUTO_OPERATION_PREFIX = "On Select: "
    }
}