package me.matrix4f.cardcutter.util

var startTime = 0L

fun startTime() {
    startTime = System.currentTimeMillis()
}

fun recordTime(indicator: String) {
    println((System.currentTimeMillis() - startTime).toString() + "ms - " + indicator)
}