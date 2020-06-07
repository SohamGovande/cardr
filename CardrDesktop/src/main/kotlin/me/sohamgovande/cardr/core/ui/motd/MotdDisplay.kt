package me.sohamgovande.cardr.core.ui.motd

import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.Modality
import javafx.stage.Stage
import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.data.prefs.Prefs
import org.apache.commons.io.IOUtils

fun showMOTD() {
    val stage = Stage()

    val stream = CardrDesktop::class.java.getResourceAsStream("/tips/tips.json")
    @Suppress("DEPRECATION") val jsonStr = IOUtils.toString(stream)
    IOUtils.closeQuietly(stream)
    val data = JsonParser().parse(jsonStr) as JsonArray
    var tipIndex = (Math.random() * data.size()).toInt()
    var tip = data[tipIndex] as JsonObject

    val vbox = VBox()
    vbox.padding = Insets(10.0)
    vbox.spacing = 10.0

    val header = Label("Did you know?")
    header.font = Font.font(20.0)

    val information = Label(tip["information"].asString)
    information.font = Font.font(13.0)
    information.prefWidth = 400.0
    information.isWrapText = true

    val checkbox = CheckBox("Display daily Cardr tips")
    checkbox.isSelected = Prefs.get().showTips
    checkbox.selectedProperty().addListener { _, _, value ->
        Prefs.get().showTips = value
        Prefs.save()
    }

    vbox.children.addAll(header, information)

    val tipImg = tip["image"]
    if (tip.has("image") && !tipImg.isJsonNull) {
        val imageView = ImageView(CardrDesktop::class.java.getResource("/tips/${tipImg.asString}").toExternalForm())
        vbox.children.add(imageView)
    } else {
        vbox.children.add(ImageView())
    }

    val closeBtn = Button("Close")
    closeBtn.setOnAction {
        stage.close()
    }

    val nextTipBtn = Button("Next Tip")
    nextTipBtn.setOnAction {
        tipIndex++
        tipIndex %= data.size()
        tip = data[tipIndex] as JsonObject

        information.text = tip["information"].asString
        val tipImg = tip["image"]
        if (tip.has("image") && !tipImg.isJsonNull) {
            val imageView = ImageView(CardrDesktop::class.java.getResource("/tips/${tipImg.asString}").toExternalForm())
            vbox.children[2] = imageView
        } else {
            vbox.children[2] = ImageView()
        }
        stage.sizeToScene()
    }

    val optionsHbox = HBox()
    optionsHbox.spacing = 10.0
    optionsHbox.children.addAll(nextTipBtn, closeBtn)

    vbox.children.addAll(checkbox, optionsHbox)

    val scene = Scene(vbox)
    scene.stylesheets.add(CardrDesktop::class.java.getResource(Prefs.get().getStylesheet()).toExternalForm())

    stage.icons.add(Image(CardrDesktop::class.java.getResourceAsStream("/icon-128.png")))
    stage.title = "Tip of the day"
    stage.scene = scene
    stage.initModality(Modality.APPLICATION_MODAL)
    stage.sizeToScene()
    stage.showAndWait()
}