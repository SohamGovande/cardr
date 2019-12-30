package me.matrix4f.cardcutter.installer.util

fun getOSType(): OS? {
    val os = System.getProperty("os.name").toLowerCase()
    if (os.contains("win")) {
        return OS.WINDOWS
    } else if (os.contains("osx")) {
        return OS.MAC
    } else if (os.contains("nix") || os.contains("aix") || os.contains("nux")) {
        return OS.LINUX
    }
    return null
}


enum class OS {
    WINDOWS, MAC, LINUX
}