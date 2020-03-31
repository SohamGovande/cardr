package me.sohamgovande.cardr.core.ui

import de.codecentric.centerdevice.MenuToolkit
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
import me.sohamgovande.cardr.util.OS
import me.sohamgovande.cardr.util.currentDate
import me.sohamgovande.cardr.util.getOSType
import me.sohamgovande.cardr.util.showInfoDialogBlocking
import java.awt.Desktop
import java.nio.file.Paths
import java.util.function.Consumer


class MenubarHelper(private val cardrUI: CardrUI, private val stage: Stage) {

    private val signInMI = MenuItem("Sign in...")
    
    fun onSuccessfulLogin() {
        signInMI.text = "Log out..."
    }

    fun apply(panel: VBox) {
        if (getOSType() == OS.WINDOWS) {
            panel.children.add(VBox(generateMenuBar()))
        } else {
            val tk = MenuToolkit.toolkit()
            val defaultApplicationMenu = tk.createDefaultApplicationMenu("cardr")
            tk.setApplicationMenu(defaultApplicationMenu)
            tk.setGlobalMenuBar(generateMenuBar())
        }
    }

    fun generateMenuBar(): MenuBar {
        val menuBar = MenuBar()

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
        historyMI.accelerator = KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN)
        historyMI.setOnAction {
            HistoryWindow().show()
        }

        accountMenu.items.add(signInMI)
        accountMenu.items.add(SeparatorMenuItem())
        accountMenu.items.add(historyMI)

        val toolsMenu = Menu("Tools")
        val copyMI = MenuItem("Copy card")
        copyMI.accelerator = KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)
        copyMI.setOnAction { cardrUI.copyCardToClipboard() }


        val refreshWordMI  = MenuItem("Refresh Word windows")
        refreshWordMI.accelerator = KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN)
        refreshWordMI.setOnAction { cardrUI.refreshWordWindows() }

        val sendMI = MenuItem("Send to Word")
        sendMI.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)
        sendMI.setOnAction { cardrUI.sendCardToVerbatim() }

        val removeSelectedMI = MenuItem("Remove Selected Text")
        removeSelectedMI.accelerator = KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)
        removeSelectedMI.setOnAction { cardrUI.removeSelectedText() }

        val keepSelectedMI = MenuItem("Remove Except for Selected Text")
        keepSelectedMI.accelerator = KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN)
        keepSelectedMI.setOnAction { cardrUI.keepOnlySelectedText() }

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
            window.addOnCloseListener(Consumer {
                cardrUI.refreshHTML()
            })
            window.show()
        }

        val wordPasteMI = MenuItem("Send to Word settings...")
        wordPasteMI.setOnAction {
            SendToWordSettingsWindow().show()
        }

        val condenseMI = CheckMenuItem("Condense paragraphs")
        condenseMI.isSelected = Prefs.get().condense
        condenseMI.setOnAction {
            Prefs.get().condense = condenseMI.isSelected
            Prefs.save()
            cardrUI.refreshHTML()
        }

        val useSmallDatesMI = CheckMenuItem("Use MM-DD for ${currentDate().year}")
        useSmallDatesMI.isSelected = !Prefs.get().onlyCardYear
        useSmallDatesMI.setOnAction {
            Prefs.get().onlyCardYear = !useSmallDatesMI.isSelected
            Prefs.save()
            cardrUI.refreshHTML()
        }

        val useEtAlMI = CheckMenuItem("Use 'et al.' for >2 authors")
        useEtAlMI.isSelected = Prefs.get().useEtAl
        useEtAlMI.setOnAction {
            Prefs.get().useEtAl = useEtAlMI.isSelected
            Prefs.save()
            cardrUI.refreshHTML()
        }

        val capitalizeAuthorsMI = CheckMenuItem("Capitalize authors' names")
        capitalizeAuthorsMI.isSelected = Prefs.get().capitalizeAuthors
        capitalizeAuthorsMI.setOnAction {
            Prefs.get().capitalizeAuthors = capitalizeAuthorsMI.isSelected
            Prefs.save()
            cardrUI.refreshHTML()
        }

        val useSlashMI = CheckMenuItem("Use / instead of - in dates")
        useSlashMI.isSelected = Prefs.get().useSlashInsteadOfDash
        useSlashMI.setOnAction {
            Prefs.get().useSlashInsteadOfDash = useSlashMI.isSelected
            Prefs.save()
            cardrUI.loadDateSeparatorLabels()
            cardrUI.refreshHTML()
        }

        val endQualsWithCommaMI = CheckMenuItem("Automatically append \", \" to last author qualification")
        endQualsWithCommaMI.isSelected = Prefs.get().endQualsWithComma
        endQualsWithCommaMI.setOnAction {
            Prefs.get().endQualsWithComma = endQualsWithCommaMI.isSelected
            Prefs.save()
            cardrUI.refreshHTML()
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
            }
        }

        val showParagraphBreaksMI = CheckMenuItem("Show paragraphs breaks")
        showParagraphBreaksMI.isSelected = Prefs.get().showParagraphBreaks
        showParagraphBreaksMI.setOnAction {
            Prefs.get().showParagraphBreaks = showParagraphBreaksMI.isSelected
            Prefs.save()
            cardrUI.refreshHTML()
        }

        settingsMenu.items.add(formatMI)
        settingsMenu.items.add(wordPasteMI)
        settingsMenu.items.add(SeparatorMenuItem())

        settingsMenu.items.add(condenseMI)
        settingsMenu.items.add(useSmallDatesMI)
        settingsMenu.items.add(useEtAlMI)
        settingsMenu.items.add(endQualsWithCommaMI)
        settingsMenu.items.add(capitalizeAuthorsMI)
        settingsMenu.items.add(useSlashMI)

        settingsMenu.items.add(SeparatorMenuItem())
        settingsMenu.items.add(darkModeMI)
        settingsMenu.items.add(showParagraphBreaksMI)

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
        val logMI = MenuItem("Open Log File")
        logMI.setOnAction { Desktop.getDesktop().browse(Paths.get(System.getProperty("cardr.data.dir"), "CardrDesktopLog.txt").toFile().toURI()) }

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

        aboutMenu.items.add(SeparatorMenuItem())
        aboutMenu.items.add(creditsMI)

        menuBar.menus.add(accountMenu)
        menuBar.menus.add(toolsMenu)
        menuBar.menus.add(settingsMenu)
        menuBar.menus.add(aboutMenu)
        return menuBar
    }
}