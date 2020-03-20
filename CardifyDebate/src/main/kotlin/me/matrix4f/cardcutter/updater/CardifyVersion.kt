package me.matrix4f.cardcutter.updater

import me.matrix4f.cardcutter.util.OS
import me.matrix4f.cardcutter.util.getOSType

data class CardifyVersion(val name: String, val build: Int, val url: String, val zipWindows: String, val zipMacOS: String, val winInstallerName: String, val macInstallerName: String) {
    fun getURL(): String {
        return if (getOSType() == OS.MAC) zipMacOS else zipWindows
    }

    fun getInstallerName(): String {
        return if (getOSType() == OS.MAC) macInstallerName else winInstallerName
    }
}