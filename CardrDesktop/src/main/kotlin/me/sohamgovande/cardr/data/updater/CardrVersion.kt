package me.sohamgovande.cardr.data.updater

import me.sohamgovande.cardr.util.OS
import me.sohamgovande.cardr.util.getOSType

data class CardrVersion(
    val name: String,
    val build: Int,
    val urlWindows: String,
    val urlMacOS: String,
    val downloadFileWin: String?,
    val downloadFileMac: String?,
    val finalFileMac: String?,
    val disabledMac: Boolean?,
    val disabledWindows: Boolean?
) {
    fun isAutoUpdaterEnabled(): Boolean {
        return if (getOSType() == OS.MAC)
            disabledMac == null || !disabledMac
        else
            disabledWindows == null || !disabledWindows
    }

    fun getURL(): String {
        return if (getOSType() == OS.MAC) urlMacOS else urlWindows
    }

    fun shouldExtract(): Boolean = getOSType() == OS.MAC

    fun getDownloadFilename(): String {
        return if (getOSType() == OS.MAC) {
            downloadFileMac ?: "cardr-${name}.zip"
        }  else {
            downloadFileWin ?: "cardr-${name}.msi"
        }
    }

    fun getFinalFilename(): String {
        return if (getOSType() == OS.MAC) {
            finalFileMac ?: "cardr-${name}.pkg"
        }  else {
            downloadFileWin ?: "cardr-${name}.msi"
        }
    }
}
