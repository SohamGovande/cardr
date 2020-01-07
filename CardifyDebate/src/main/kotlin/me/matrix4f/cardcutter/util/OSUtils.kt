package me.matrix4f.cardcutter.util

fun getOSType(): OS? {
    val os = System.getProperty("os.name").toLowerCase()
    if (os.contains("win")) {
        return OS.WINDOWS
    } else if (os.contains("osx") || os.contains("mac") || os.contains("os x")) {
        return OS.MAC
    } else if (os.contains("nix") || os.contains("aix") || os.contains("nux")) {
        return OS.LINUX
    }
    return null
}


fun getProcessorBits(): Int {
    return System.getProperty("sun.arch.data.model", "32").toInt()
}

enum class OS {
    WINDOWS, MAC, LINUX
}