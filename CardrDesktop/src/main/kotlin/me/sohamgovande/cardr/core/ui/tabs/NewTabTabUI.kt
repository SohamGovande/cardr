package me.sohamgovande.cardr.core.ui.tabs

import me.sohamgovande.cardr.core.ui.CardrUI

class NewTabTabUI(cardrUI: CardrUI) : TabUI("+", cardrUI) {

    override fun generate() {
        internalTab.isClosable = false
    }

    override fun onTabSelected() {
        cardrUI.createNewEditTab(null)
    }
}