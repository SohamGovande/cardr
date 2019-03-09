package me.matrix4f.cardcutter.prefs.windows

import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage

abstract class PrefsWindow(val title: String) {

    protected val window = Stage()

    fun show() {
        window.initModality(Modality.APPLICATION_MODAL)
        window.title = title
        window.scene = generateUI()
        window.isResizable = false
        window.show()
        window.setOnCloseRequest {
            close()
        }
    }

    open fun close() {
        window.close()
    }

    protected abstract fun generateUI(): Scene
}