package me.matrix4f.cardcutter.ui.style

import com.sun.javafx.scene.control.skin.ButtonSkin
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.beans.InvalidationListener
import javafx.scene.control.Button
import javafx.scene.effect.ColorAdjust
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.util.Duration

class XButtonSkin(control: Button): ButtonSkin(control) {

    init {
        val onHoverColorChange = ColorAdjust()
        onHoverColorChange.brightness = 0.0

        control.effect = onHoverColorChange

        control.setOnMouseEntered {
            // Background color
            val bg = Timeline(
                KeyFrame(Duration.seconds(0.0), KeyValue(onHoverColorChange.brightnessProperty(), onHoverColorChange.brightness, Interpolator.LINEAR)),
                KeyFrame(Duration.seconds(ANIM_LENGTH), KeyValue(onHoverColorChange.brightnessProperty(), -HOVER_DARKNESS, Interpolator.LINEAR))
            )
            bg.cycleCount = 1
            bg.isAutoReverse = false
            bg.play()
        }
        control.setOnMouseExited {
            val bg = Timeline(
                KeyFrame(Duration.seconds(0.0), KeyValue(onHoverColorChange.brightnessProperty(), onHoverColorChange.brightness, Interpolator.LINEAR)),
                KeyFrame(Duration.seconds(ANIM_LENGTH), KeyValue(onHoverColorChange.brightnessProperty(), 0, Interpolator.LINEAR))
            )
            bg.cycleCount = 1
            bg.isAutoReverse = false
            bg.play()
        }
    }

    companion object {
        const val HOVER_DARKNESS = 0.05
        const val ANIM_LENGTH = 0.1
    }
}