package me.matrix4f.cardcutter.util

import javafx.application.Platform
import javafx.scene.control.Alert

var startTime = 0L

fun showErrorDialog(brief: String, full: String) {
    Platform.runLater {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Error"
        alert.headerText = brief
        alert.contentText = full
        alert.showAndWait()
    }
}

fun startTime() {
    startTime = System.currentTimeMillis()
}

fun recordTime(indicator: String) {
    println((System.currentTimeMillis() - startTime).toString() + "ms - " + indicator)
}