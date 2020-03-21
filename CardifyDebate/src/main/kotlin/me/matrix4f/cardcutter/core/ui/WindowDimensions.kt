package me.matrix4f.cardcutter.core.ui

import javafx.stage.Screen
import javafx.stage.Stage

data class WindowDimensions(val x: Double, val y: Double, val w: Double, val h: Double, val maximized: Boolean) {

    constructor(stage: Stage) : this(stage.x, stage.y, stage.width, stage.height, stage.isMaximized)

    fun apply(stage: Stage) {
        if (!maximized) {
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