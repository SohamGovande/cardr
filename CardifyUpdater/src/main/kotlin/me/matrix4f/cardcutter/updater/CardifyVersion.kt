package me.matrix4f.cardcutter.updater

data class CardifyVersion(val name: String, val build: Int, val url: String, val requiresFullUpdate: Boolean, val patchZip: String, val fullZip: String)