package me.sohamgovande.cardr.data.files

import java.util.*

data class FSFolder(var lastCardrVersion: Int, var path: String, var cardUUIDs: MutableList<UUID>) {

    fun getCards(): MutableList<CardData> =
        cardUUIDs.map { CardrFileSystem.findCard(it) }.toMutableList()

    fun isRootFolder(): Boolean = !path.contains('/')

    fun getParentFolder(): String {
        if (path == "/")
            return ""

        val slash = path.lastIndexOf('/')
        if (slash == -1)
            return ""

        return path.substring(0, slash)
    }

    fun getName(): String {
        if (path == "/")
            return "Saved Cards"

        val slash = path.lastIndexOf('/')
        return if (slash == -1)
            path
        else
            path.substring(slash+1)
    }

    fun getChildren(directChildren: Boolean): List<FSFolder> = CardrFileSystem.folders.filter { if (directChildren) it.getParentFolder() == path else it.path.startsWith("$path/")}.toList()

    override fun toString(): String = getName()
}