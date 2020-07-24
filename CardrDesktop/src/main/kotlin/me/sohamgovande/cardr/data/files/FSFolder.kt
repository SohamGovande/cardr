package me.sohamgovande.cardr.data.files

import java.util.*

data class FSFolder(var lastCardrVersion: Int, var path: String, var cardUUIDs: MutableList<UUID>) {

    fun getCards(): MutableList<CardData> =
        cardUUIDs.map { CardrFileSystem.findCard(it) }.toMutableList()
}