package me.sohamgovande.cardr.core.ui.windows

import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.WindowEvent
import java.util.function.Consumer

abstract class ModalWindow(val title: String) {

    protected val window = Stage()
    protected val onCloseListeners: ArrayList<Consumer<HashMap<String, Any>>> = arrayListOf()

    protected var onCloseData: HashMap<String, Any> = hashMapOf()
    var autoRemove = true
    var forcedClose = false

    fun show() {
        window.initModality(Modality.APPLICATION_MODAL)
        window.title = title
        window.scene = generateUI()
        window.isResizable = true
        window.sizeToScene()
        window.show()

        window.setOnCloseRequest {
            close(it)
            for (listener in onCloseListeners) {
                listener.accept(onCloseData)
            }
        }

        openWindows.add(this)
    }

    fun addOnCloseListener(listener: Consumer<HashMap<String, Any>>) {
        onCloseListeners.add(listener)
    }

    open fun close(event: WindowEvent?) {
        window.close()
        if (event == null || !event.isConsumed) {
            removeFromList()
        }
    }

    private fun removeFromList() {
        if (autoRemove)
            openWindows.remove(this)
    }


    protected abstract fun generateUI(): Scene

    companion object {
        val openWindows = mutableListOf<ModalWindow>()
    }
}