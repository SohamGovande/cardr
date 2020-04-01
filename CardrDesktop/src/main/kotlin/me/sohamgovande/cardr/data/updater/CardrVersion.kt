package me.sohamgovande.cardr.data.updater

import me.sohamgovande.cardr.util.OS
import me.sohamgovande.cardr.util.getOSType

data class CardrVersion(val name: String, val build: Int, val urlWindows: String, val urlMacOS: String) {
    fun getURL(): String {
        return if (getOSType() == OS.MAC) urlMacOS else urlWindows
    }

    fun getInstallerName(): String {
        return if (getOSType() == OS.MAC) "cardr-${name}.pkg" else "cardr-${name}.msi"
    }
}
