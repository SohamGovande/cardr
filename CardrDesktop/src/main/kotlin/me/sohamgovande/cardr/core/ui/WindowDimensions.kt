package me.sohamgovande.cardr.core.ui

import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle

data class WindowDimensions(var x: Double, var y: Double, var w: Double, var h: Double, var maximized: Boolean) {

    constructor(stage: Stage) : this(stage.x, stage.y, stage.width, stage.height, stage.isMaximized)

    fun apply(stage: Stage) {
        if (!maximized || stage.style == StageStyle.TRANSPARENT) {
            stage.x = x
            stage.y = y
            stage.width = w
            stage.height = h
        } else {
            val screen = Screen.getPrimary()
            val bounds = screen.visualBounds
            stage.width = bounds.width
            stage.height = bounds.height
        }
        stage.isMaximized = maximized
    }
}
