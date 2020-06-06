package me.sohamgovande.cardr.core.ui

import de.codecentric.centerdevice.MenuToolkit
import javafx.application.Platform
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.VBox
import javafx.stage.Stage
import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.core.ui.windows.*
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.data.urls.UrlHelper
import me.sohamgovande.cardr.util.*
import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Paths
import java.util.function.Consumer


class MenubarHelper(private val cardrUI: CardrUI, private val stage: Stage) {

    private val signInMI = MenuItem("Sign in...")
    val hidePlainPasteWarningMI = CheckMenuItem("Hide plaintext paste dialog")
    val hideFormattingWarningMI = CheckMenuItem("Hide highlight/underline dialog")
    val hideCopyPasteWarningMI = CheckMenuItem("Hide copy/paste dialog")
    val hideUpdateWarningMI = CheckMenuItem("Hide update dialog")
    val removeSelectedMI = MenuItem("Remove Selected Text")
    val keepSelectedMI = MenuItem("Remove Except for Selected Text")

    private val ctrlKeyMask: KeyCombination.Modifier 
        get() = if (getOSType() == OS.MAC) KeyCombination.META_DOWN else KeyCombination.CONTROL_DOWN

    fun onSuccessfulLogin() {
        Platform.runLater { signInMI.text = "Log out..." }
    }

    fun applyDefaultMenu(panel: VBox) {
        panel.children.add(VBox(generateMenuBar()))
    }

    private fun reopenMenu(settingsMenu: Menu) {
        Thread { Platform.runLater { settingsMenu.show() } }.start()
    }

    fun applyMacMenu() {
        val tk = MenuToolkit.toolkit()
        val appMenu = Menu("cardr")
        val appMenuItem1 = MenuItem("Welcome to cardr!")
        appMenu.items.add(appMenuItem1)
        tk.setApplicationMenu(appMenu)
        tk.setGlobalMenuBar(generateMenuBar())
    }

    fun generateMenuBar(): MenuBar {
        val menuBar = MenuBar()

        val macApplicationMenu: Menu
        if (getOSType() == OS.MAC) {
            val tk = MenuToolkit.toolkit()
            macApplicationMenu = Menu("cardr", null, tk.createHideMenuItem("cardr"), tk.createHideOthersMenuItem(),
                    tk.createUnhideAllMenuItem(), SeparatorMenuItem(), tk.createQuitMenuItem("cardr"))
        } else {
            macApplicationMenu = Menu()
        }
        val accountMenu = Menu("Account")
        
        signInMI.setOnAction {
            Prefs.get().encryptedPassword = ""
            Prefs.get().emailAddress = ""
            Prefs.get().accessToken = ""
            Prefs.save()
            signInMI.text = "Sign in..."
            val signInWindow = SignInWindow(SignInLauncherOptions.MANUAL_SIGNIN, cardrUI.currentUser)
            signInWindow.show()
        }
        val historyMI = MenuItem("Card History")
        historyMI.accelerator = KeyCodeCombination(KeyCode.H, ctrlKeyMask)
        historyMI.setOnAction {
            HistoryWindow(cardrUI).show()
        }

        accountMenu.items.add(signInMI)
        accountMenu.items.add(SeparatorMenuItem())
        accountMenu.items.add(historyMI)

        val toolsMenu = Menu("Tools")
        val copyMI = MenuItem("Copy card")
        copyMI.accelerator = KeyCodeCombination(KeyCode.C, ctrlKeyMask, KeyCombination.SHIFT_DOWN)
        copyMI.setOnAction { cardrUI.toolsUI.copyCardToClipboard() }

        val refreshWordMI  = MenuItem("Refresh Word windows")
        refreshWordMI.accelerator = KeyCodeCombination(KeyCode.R, ctrlKeyMask)
        refreshWordMI.setOnAction { cardrUI.toolsUI.refreshWordWindows() }

        val sendMI = MenuItem("Send to Word")
        sendMI.accelerator = KeyCodeCombination(KeyCode.S, ctrlKeyMask)
        sendMI.setOnAction { cardrUI.toolsUI.sendCardToWord() }

        removeSelectedMI.accelerator = KeyCodeCombination(KeyCode.X, ctrlKeyMask, KeyCombination.SHIFT_DOWN)
        removeSelectedMI.setOnAction { cardrUI.toolsUI.removeSelectedText() }

        keepSelectedMI.accelerator = KeyCodeCombination(KeyCode.X, ctrlKeyMask, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN)
        keepSelectedMI.setOnAction { cardrUI.toolsUI.keepOnlySelectedText() }

        toolsMenu.items.add(copyMI)
        toolsMenu.items.add(refreshWordMI)
        toolsMenu.items.add(sendMI)
        toolsMenu.items.add(SeparatorMenuItem())
        toolsMenu.items.add(removeSelectedMI)
        toolsMenu.items.add(keepSelectedMI)

        val settingsMenu = Menu("Settings")

        val formatMI = MenuItem("Card and cite format settings...")
        formatMI.setOnAction {
            val window = FormatPrefsWindow()
            window.addOnCloseListener{
                cardrUI.refreshHTML()
            }
            window.show()
        }

        val wordPasteMI = MenuItem("Send to Word settings...")
        wordPasteMI.setOnAction {
            SendToWordSettingsWindow(cardrUI).show()
        }

        val highlightUnderlineMI = MenuItem("Highlight & underline settings...")
        highlightUnderlineMI.setOnAction {
            MarkupCardSettingsWindow().show()
        }

        val condenseMI = CheckMenuItem("Condense paragraphs")
        condenseMI.isSelected = Prefs.get().condense
        condenseMI.setOnAction {
            Prefs.get().condense = condenseMI.isSelected
            Prefs.save()
            cardrUI.refreshHTML()
            reopenMenu(settingsMenu)
        }

        val useSmallDatesMI = CheckMenuItem("Use MM-DD for ${currentDate().year}")
        useSmallDatesMI.isSelected = !Prefs.get().onlyCardYear
        useSmallDatesMI.setOnAction {
            Prefs.get().onlyCardYear = !useSmallDatesMI.isSelected
            Prefs.save()
            cardrUI.refreshHTML()
            reopenMenu(settingsMenu)
        }

        val useEtAlMI = CheckMenuItem("Use 'et al.' for >2 authors")
        useEtAlMI.isSelected = Prefs.get().useEtAl
        useEtAlMI.setOnAction {
            Prefs.get().useEtAl = useEtAlMI.isSelected
            Prefs.save()
            cardrUI.refreshHTML()
            reopenMenu(settingsMenu)
        }

        val capitalizeAuthorsMI = CheckMenuItem("Make authors' names ALL CAPS")
        capitalizeAuthorsMI.isSelected = Prefs.get().capitalizeAuthors
        capitalizeAuthorsMI.setOnAction {
            Prefs.get().capitalizeAuthors = capitalizeAuthorsMI.isSelected
            Prefs.save()
            cardrUI.refreshHTML()
            reopenMenu(settingsMenu)
        }

        val useSlashMI = CheckMenuItem("Use / instead of - as date separator")
        useSlashMI.isSelected = Prefs.get().useSlashInsteadOfDash
        useSlashMI.setOnAction {
            Prefs.get().useSlashInsteadOfDash = useSlashMI.isSelected
            Prefs.save()
            cardrUI.loadDateSeparatorLabels()
            cardrUI.refreshHTML()
            reopenMenu(settingsMenu)
        }

        val endQualsWithCommaMI = CheckMenuItem("Automatically add \", \" to last author qual")
        endQualsWithCommaMI.isSelected = Prefs.get().endQualsWithComma
        endQualsWithCommaMI.setOnAction {
            Prefs.get().endQualsWithComma = endQualsWithCommaMI.isSelected
            Prefs.save()
            cardrUI.refreshHTML()
            reopenMenu(settingsMenu)
        }

        val darkModeMI = CheckMenuItem("Night mode")
        darkModeMI.isSelected = Prefs.get().darkMode
        darkModeMI.setOnAction {
            Prefs.get().darkMode = darkModeMI.isSelected
            Prefs.save()

            stage.scene.stylesheets.remove("/styles.css")
            stage.scene.stylesheets.remove("/styles-dark.css")
            stage.scene.stylesheets.add(javaClass.getResource(Prefs.get().getStylesheet()).toExternalForm())
            cardrUI.refreshHTML()
            cardrUI.loadMenuIcons()

            if (!Prefs.get().darkMode) {
                val alert = Alert(Alert.AlertType.INFORMATION)
                alert.title = "Please restart cardr"
                alert.headerText = "Please restart cardr for the changes to take effect."
                alert.contentText = "Upon restart, your theme changes will be applied."
                alert.showAndWait()
            } else {
                reopenMenu(settingsMenu)
            }
        }

        val showParagraphBreaksMI = CheckMenuItem("Show paragraph breaks")
        showParagraphBreaksMI.isSelected = Prefs.get().showParagraphBreaks
        showParagraphBreaksMI.setOnAction {
            Prefs.get().showParagraphBreaks = showParagraphBreaksMI.isSelected
            Prefs.save()
            cardrUI.refreshHTML()
            reopenMenu(settingsMenu)
        }

        val messagesMenu = Menu("Messages")
        hidePlainPasteWarningMI.isSelected = Prefs.get().hidePastePlainTextDialog
        hidePlainPasteWarningMI.setOnAction {
            Prefs.get().hidePastePlainTextDialog = hidePlainPasteWarningMI.isSelected
            Prefs.save()
            reopenMenu(settingsMenu)
            reopenMenu(messagesMenu)
        }
        hideCopyPasteWarningMI.isSelected = Prefs.get().hideCopyDialog
        hideCopyPasteWarningMI.setOnAction {
            Prefs.get().hideCopyDialog = hideCopyPasteWarningMI.isSelected
            Prefs.save()
            reopenMenu(settingsMenu)
            reopenMenu(messagesMenu)
        }
        hideUpdateWarningMI.isSelected = Prefs.get().hideUpdateDialog
        hideUpdateWarningMI.setOnAction {
            Prefs.get().hideUpdateDialog = hideUpdateWarningMI.isSelected
            Prefs.save()
            reopenMenu(settingsMenu)
            reopenMenu(messagesMenu)
        }
        hideFormattingWarningMI.isSelected = Prefs.get().hideFormattingDialog
        hideFormattingWarningMI.setOnAction {
            Prefs.get().hideFormattingDialog = hideFormattingWarningMI.isSelected
            Prefs.save()
            reopenMenu(settingsMenu)
            reopenMenu(messagesMenu)
        }
        messagesMenu.items.add(hidePlainPasteWarningMI)
        messagesMenu.items.add(hideCopyPasteWarningMI)
        messagesMenu.items.add(hideUpdateWarningMI)

        settingsMenu.items.add(formatMI)
        settingsMenu.items.add(wordPasteMI)
        settingsMenu.items.add(highlightUnderlineMI)
        settingsMenu.items.add(SeparatorMenuItem())

        settingsMenu.items.add(condenseMI)
        settingsMenu.items.add(useSmallDatesMI)
        settingsMenu.items.add(useEtAlMI)
        settingsMenu.items.add(endQualsWithCommaMI)
        settingsMenu.items.add(capitalizeAuthorsMI)
        settingsMenu.items.add(useSlashMI)
        settingsMenu.items.add(showParagraphBreaksMI)

        settingsMenu.items.add(SeparatorMenuItem())
        settingsMenu.items.add(darkModeMI)
        settingsMenu.items.add(messagesMenu)

        val aboutMenu = Menu("About")

        val creditsMI = MenuItem("Credits")
        creditsMI.setOnAction { CreditsWindow().show() }
        val donateMI = MenuItem("Support development/donate")
        donateMI.setOnAction { UrlHelper.browse("donate") }

        val websiteMI = MenuItem("Visit website")
        websiteMI.setOnAction { UrlHelper.browse("homepage") }
        val chromeMI = MenuItem("Get Chrome Extension")
        chromeMI.setOnAction { UrlHelper.browse("extension") }
        val githubMI = MenuItem("Contribute on GitHub")
        githubMI.setOnAction { UrlHelper.browse("github") }
        val viewSupportedSites = MenuItem("View supported websites")
        viewSupportedSites.setOnAction {
            showInfoDialogBlocking("Supported websites", "Once you click OK, cardr will display a list of supported websites. These are NOT the only websites you can use cardr on, but are the ones that cardr offers 100% integration for. For other websites that aren't on this list, cardr will still work most of the time.")
            UrlHelper.browse("supportedSitesSpreadsheet")
        }

        val versionMI = MenuItem("Version")
        versionMI.setOnAction { showInfoDialogBlocking("Cardr is running version ${CardrDesktop.CURRENT_VERSION}.", "") }
        val helpMI = MenuItem("Help & FAQs")
        helpMI.setOnAction { UrlHelper.browse("faq") }
        val logMI = MenuItem("Open Log")
        logMI.setOnAction { Desktop.getDesktop().browse(Paths.get(System.getProperty("cardr.data.dir"), "CardrDesktopLog.txt").toFile().toURI()) }
        val sendToDeveloperMI = MenuItem("Send Log to Developer")
        sendToDeveloperMI.setOnAction {
            val log = Files.readString(Paths.get(System.getProperty("cardr.data.dir"), "CardrDesktopLog.txt"))
            sendToDeveloper(log, "Cardr Log")
        }

        aboutMenu.items.add(donateMI)

        aboutMenu.items.add(SeparatorMenuItem())
        aboutMenu.items.add(websiteMI)
        aboutMenu.items.add(chromeMI)
        aboutMenu.items.add(githubMI)
        aboutMenu.items.add(viewSupportedSites)

        aboutMenu.items.add(SeparatorMenuItem())
        aboutMenu.items.add(helpMI)
        aboutMenu.items.add(versionMI)
        aboutMenu.items.add(logMI)
        aboutMenu.items.add(sendToDeveloperMI)

        aboutMenu.items.add(SeparatorMenuItem())
        aboutMenu.items.add(creditsMI)

        if (getOSType() == OS.MAC) { menuBar.menus.add(macApplicationMenu) }
        menuBar.menus.add(accountMenu)
        menuBar.menus.add(toolsMenu)
        menuBar.menus.add(settingsMenu)
        menuBar.menus.add(aboutMenu)
        return menuBar
    }
}
