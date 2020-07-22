package me.sohamgovande.cardr.core.ui

import javafx.application.Platform
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.core.auth.CardrUser
import me.sohamgovande.cardr.core.ui.property.TitleCardProperty
import me.sohamgovande.cardr.core.ui.tabs.EditCardTabUI
import me.sohamgovande.cardr.core.ui.tabs.NewTabTabUI
import me.sohamgovande.cardr.core.ui.tabs.TabUI
import me.sohamgovande.cardr.core.ui.windows.FormatPrefsWindow
import me.sohamgovande.cardr.core.ui.windows.SignInLauncherOptions
import me.sohamgovande.cardr.core.ui.windows.SignInWindow
import me.sohamgovande.cardr.core.ui.windows.ocr.OCRCardBuilderWindow
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.data.updater.UpdateChecker
import me.sohamgovande.cardr.util.OS
import me.sohamgovande.cardr.util.getOSType
import me.sohamgovande.cardr.util.showInfoDialogBlocking
import org.apache.logging.log4j.LogManager


class CardrUI(val stage: Stage) {

    private val panel = VBox()
    val menubarHelper = MenubarHelper(this, stage)

    val tabs = mutableListOf<TabUI>()
    private val tabPane = TabPane()
    var currentUser = CardrUser()

    var ocrCardBuilderWindow: OCRCardBuilderWindow? = null

    var finishedInitialLoad = false
    var finishedDeferredLoad = false

    init {
        currentUser.onSuccessfulLogin = menubarHelper::onSuccessfulLogin
    }

    fun initialize(): VBox {
        stage.widthProperty().addListener { _, _, _ -> onWindowResized() }
        stage.heightProperty().addListener { _, _, _ -> onWindowResized() }

        if (getOSType() == OS.WINDOWS) {
            logger.info("Generating default menu bar")
            menubarHelper.applyDefaultMenu(panel)
        }

        tabs.add(EditCardTabUI(this))
        tabs.add(EditCardTabUI(this))
        tabs.add(NewTabTabUI(this))
        for (tab in tabs) {
            tab.generate()
            tab.addToTabPane(tabPane, false)
        }
        tabPane.selectionModel.select(0)
        tabPane.selectionModel.selectedItemProperty().addListener { _, oldTab, newTab: Tab? ->
            if (newTab != null) {
                selectNewTab(mapToTabUI(oldTab)!!, mapToTabUI(newTab)!!)
            }
        }
        tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS
        panel.children.add(tabPane)

        updateTabClosingPolicy()

        Thread {
            logger.info("Checking login status")
            checkLoginStatus()

            logger.info("Checking for updates")
            checkForUpdates()
        }.start()
        finishedInitialLoad = true

        return panel
    }

    fun doDeferredLoad() {
        for (tab in tabs)
            tab.doDeferredLoad()
        finishedDeferredLoad = true
    }

    fun loadMenuIcons() {
        for (tab in tabs)
            tab.loadIcons()
    }


    fun visitURL(url: String) {
        if (url == "ocr")
            return
        Platform.runLater {
            if (!Prefs.get().hasCutCard) {
                showInfoDialogBlocking("Want to edit the card format?", "Cardr allows you to edit virtually every aspect of the cards you cut—if you're not satisfied with the default citation format, feel free to tweak it to your needs using Settings > Card and cite format settings...", "Change card/citation format") {
                    FormatPrefsWindow(this, getSelectedTab(EditCardTabUI::class.java)!!.propertyManager).show()
                }
                Prefs.get().hasCutCard = true
                Prefs.save()
            }
        }
        Thread {
            currentUser.visitWebsite(url)
        }.start()
    }

    private fun checkForUpdates() {
        UpdateChecker(this).checkForUpdates()
        logger.info("Initializing Word windows")
//        toolsUI.refreshWordWindows() TODO: add this back
    }

    private fun checkLoginStatus() {
        if ((CardrDesktop.IS_FIRST_LAUNCH && CardrDesktop.WAS_FIRST_LAUNCH_SUCCESSFUL) || CardrDesktop.OVERRIDE_LOGIN_CHECK)
            return
        if (Prefs.get().emailAddress.isEmpty()
            || Prefs.get().accessToken.isEmpty()) {
            // Needs to sign in
            logger.info("User needs to sign in - first time")
            Platform.runLater { SignInWindow(SignInLauncherOptions.WELCOME, currentUser, this).show() }
        } else {
            val renewResult = currentUser.renew()
            if (!renewResult.wasSuccessful()) {
                logger.info("User needs to sign in - token expired")
                // Access token has expired
                Platform.runLater { SignInWindow(SignInLauncherOptions.TOKEN_EXPIRED, currentUser, this).show() }
            } else {
                logger.info("Successfully renewed login token")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : TabUI>getSelectedTab(clazz: Class<T>): T? {
        val selectedTab = mapToTabUI(tabPane.selectionModel.selectedItem)
        if (selectedTab == null || (selectedTab.javaClass != clazz && clazz != TabUI::class.java) && selectedTab.isAlive)
            return null
        return selectedTab as T
    }

    fun <T : TabUI>getTabsByClass(clazz: Class<T>): List<T> {
        return tabs.filter { it.javaClass == clazz && it.isAlive }
            .map {
                @Suppress("UNCHECKED_CAST")
                it as T
            }
            .toList()
    }

    fun refreshAllHTML() {
        for (tab in getTabsByClass(EditCardTabUI::class.java))
            tab.refreshHTML()
    }

    fun onWindowResized() {
        for (tab in tabs)
            tab.onWindowResized()
    }

    fun updateWindowTitle(title: String) {
        Platform.runLater {
            var trimmed = title.substring(0, title.length.coerceAtMost(100))
            if (title.length >= 100)
                trimmed += "..."
            stage.title = "$trimmed - cardr ${CardrDesktop.CURRENT_VERSION}"
        }
    }

    fun updateTabClosingPolicy() {
        val otherTabs = getTabsByClass(EditCardTabUI::class.java)
        if (otherTabs.size == 1) {
            otherTabs[0].internalTab.isClosable = false
        } else {
            for (tab in otherTabs)
                tab.internalTab.isClosable = true
        }
    }

    private fun selectNewTab(oldTab: TabUI, newTab: TabUI) {
        if (newTab is EditCardTabUI) {
            val title = newTab.propertyManager.getByName<TitleCardProperty>("Title")!!.getValue()
            updateWindowTitle(if (title.isBlank()) "Card Editor" else title)
        } else if (newTab !is NewTabTabUI) {
            updateWindowTitle(newTab.internalTab.text)
        }
    }

    fun createNewEditTab(url: String?) {
        val tab = EditCardTabUI(this)
        tabs.add(tabs.size - 1, tab)

        tab.generate()
        tab.doDeferredLoad()
        tab.onWindowResized()
        tab.addToTabPane(tabPane, true)
        tabPane.selectionModel.select(tab.internalTab)

        if (url != null) {
            tab.urlTF.text = url
            tab.gotoUrlBtn.fire()
        }

        updateTabClosingPolicy()
    }

    private fun mapToTabUI(tab: Tab): TabUI? {
        return tabs.firstOrNull { it.internalTab == tab }
    }

    companion object {
        private val logger = LogManager.getLogger(CardrUI::class.java)
    }
}
