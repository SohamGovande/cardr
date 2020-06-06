package me.sohamgovande.cardr.core.ui.windows.ocr

import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.input.MouseEvent
import javafx.stage.Stage

fun addResizeListener(stage: Stage) {
    val resizeListener = ResizeListener(stage)
    stage.scene.addEventHandler(MouseEvent.MOUSE_MOVED, resizeListener)
    stage.scene.addEventHandler(MouseEvent.MOUSE_PRESSED, resizeListener)
    stage.scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, resizeListener)
    stage.scene.addEventHandler(MouseEvent.MOUSE_EXITED, resizeListener)
    stage.scene.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, resizeListener)
    val children = stage.scene.root.childrenUnmodifiable
    for (child in children) {
        addListenerDeeply(child, resizeListener)
    }
}

fun addListenerDeeply(node: Node, listener: EventHandler<MouseEvent>?) {
    node.addEventHandler(MouseEvent.MOUSE_MOVED, listener)
    node.addEventHandler(MouseEvent.MOUSE_PRESSED, listener)
    node.addEventHandler(MouseEvent.MOUSE_DRAGGED, listener)
    node.addEventHandler(MouseEvent.MOUSE_EXITED, listener)
    node.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, listener)
    if (node is Parent) {
        val children = node.childrenUnmodifiable
        for (child in children) {
            addListenerDeeply(child, listener)
        }
    }
}

class ResizeListener(private val stage: Stage) : EventHandler<MouseEvent> {
    private var cursorEvent = Cursor.DEFAULT
    private var startX = 0.0
    private var startY = 0.0
    override fun handle(mouseEvent: MouseEvent) {
        val mouseEventType = mouseEvent.eventType
        val scene = stage.scene
        val mouseEventX = mouseEvent.sceneX
        val mouseEventY = mouseEvent.sceneY
        val sceneWidth = scene.width
        val sceneHeight = scene.height
        if (MouseEvent.MOUSE_MOVED == mouseEventType) {
            cursorEvent = if (mouseEventX < BORDER_SIZE && mouseEventY < BORDER_SIZE) {
                Cursor.NW_RESIZE
            } else if (mouseEventX < BORDER_SIZE && mouseEventY > sceneHeight - BORDER_SIZE) {
                Cursor.SW_RESIZE
            } else if (mouseEventX > sceneWidth - BORDER_SIZE && mouseEventY < BORDER_SIZE) {
                Cursor.NE_RESIZE
            } else if (mouseEventX > sceneWidth - BORDER_SIZE && mouseEventY > sceneHeight - BORDER_SIZE) {
                Cursor.SE_RESIZE
            } else if (mouseEventX < BORDER_SIZE) {
                Cursor.W_RESIZE
            } else if (mouseEventX > sceneWidth - BORDER_SIZE) {
                Cursor.E_RESIZE
            } else if (mouseEventY < BORDER_SIZE) {
                Cursor.N_RESIZE
            } else if (mouseEventY > sceneHeight - BORDER_SIZE) {
                Cursor.S_RESIZE
            } else {
                Cursor.DEFAULT
            }
            scene.cursor = cursorEvent
        } else if (MouseEvent.MOUSE_EXITED == mouseEventType || MouseEvent.MOUSE_EXITED_TARGET == mouseEventType) {
            scene.cursor = Cursor.DEFAULT
        } else if (MouseEvent.MOUSE_PRESSED == mouseEventType) {
            startX = stage.width - mouseEventX
            startY = stage.height - mouseEventY
        } else if (MouseEvent.MOUSE_DRAGGED == mouseEventType) {
            if (Cursor.DEFAULT != cursorEvent) {
                if (!(Cursor.W_RESIZE == cursorEvent || Cursor.E_RESIZE == cursorEvent)) {
                    val minHeight = if (stage.minHeight > BORDER_SIZE * 2) stage.minHeight else BORDER_SIZE * 2.0
                    if (Cursor.NW_RESIZE == cursorEvent || Cursor.N_RESIZE == cursorEvent || Cursor.NE_RESIZE == cursorEvent) {
                        if (stage.height > minHeight || mouseEventY < 0) {
                            stage.height = stage.y - mouseEvent.screenY + stage.height
                            stage.y = mouseEvent.screenY
                        }
                    } else {
                        if (stage.height > minHeight || mouseEventY + startY - stage.height > 0) {
                            stage.height = mouseEventY + startY
                        }
                    }
                }
                if (Cursor.N_RESIZE != cursorEvent && Cursor.S_RESIZE != cursorEvent) {
                    val minWidth = if (stage.minWidth > BORDER_SIZE * 2) stage.minWidth else BORDER_SIZE * 2.0
                    if (Cursor.NW_RESIZE == cursorEvent || Cursor.W_RESIZE == cursorEvent || Cursor.SW_RESIZE == cursorEvent) {
                        if (stage.width > minWidth || mouseEventX < 0) {
                            stage.width = stage.x - mouseEvent.screenX + stage.width
                            stage.x = mouseEvent.screenX
                        }
                    } else {
                        if (stage.width > minWidth || mouseEventX + startX - stage.width > 0) {
                            stage.width = mouseEventX + startX
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val BORDER_SIZE = 10.0
    }
}