package me.sohamgovande.cardr.core.ui.windows.ocr

import com.google.gson.JsonParser
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.stage.StageStyle
import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.core.auth.SecretData
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.core.ui.WindowDimensions
import me.sohamgovande.cardr.core.ui.windows.ModalWindow
import me.sohamgovande.cardr.core.ui.windows.ocr.ResizeListener.Companion.BORDER_SIZE
import me.sohamgovande.cardr.data.prefs.Prefs
import org.apache.commons.text.StringEscapeUtils
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClientBuilder
import java.awt.Rectangle
import java.awt.Robot
import java.io.BufferedReader
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.JarFile
import javax.imageio.ImageIO
import kotlin.math.abs

class OCRSelectionWindow(private val cardrUI: CardrUI): ModalWindow("OCR Region"){

    private val menuBox = HBox()
    private val captureBtn = Button("Capture")

    private var xOffset = 0.0
    private var yOffset = 0.0

    private var thread: Thread? = null

    override fun show() {
        hideAllWindows()
        window.initStyle(StageStyle.TRANSPARENT)
        window.title = title
        window.scene = generateUI()
        window.isResizable = true
        window.isAlwaysOnTop = true
        window.show()

        window.setOnCloseRequest {
            close(it)
            for (listener in onCloseListeners) {
                listener(onCloseData)
            }
        }

        addResizeListener(window)
        openWindows.add(this)

        val windowDimensions = Prefs.get().ocrWindowDimensions
        if (windowDimensions.x != -1024.1024) {
            windowDimensions.apply(window)
        }
    }

    private fun onCapture() {
        Prefs.get().ocrWindowDimensions = WindowDimensions(window)
        Prefs.save()

        window.hide()
        val robot = Robot()
        val image = robot.createScreenCapture(Rectangle(
            window.x.toInt(), window.y.toInt(), window.width.toInt(), window.height.toInt()
        ))
        val imageFile = Paths.get(System.getProperty("cardr.data.dir"), "ocr", "ocr-region.png").toFile()
        ImageIO.write(image, "png", imageFile)
        window.show()
        captureBtn.graphic = null
        captureBtn.text = "Loading..."
        captureBtn.isDisable = true

        thread = Thread {
            val text = getOCRFromAPI()
            Platform.runLater {
                close(null)
                showAllWindows()

                val builder = if (cardrUI.ocrCardBuilderWindow == null) OCRCardBuilderWindow(cardrUI) else cardrUI.ocrCardBuilderWindow!!
                if (cardrUI.ocrCardBuilderWindow == null) {
                    cardrUI.ocrCardBuilderWindow = builder
                    builder.show()
                } else {
                    builder.window.requestFocus()
                }
                builder.addOnCloseListener(this::loadOCRText)
                builder.importText(text)
            }
        }
        thread!!.start()
    }

    private fun getOCRFromAPI(): String {
        val classLoader = URLClassLoader(arrayOf(Paths.get(System.getProperty("cardr.data.dir"), "ocr", "CardrOCR.jar").toFile().toURL()), ClassLoader.getSystemClassLoader())
        val ocrClass = classLoader.loadClass("me.sohamgovande.cardr.ocr.CardrOCR")
        ocrClass.getMethod("doOCR").invoke(
            ocrClass.getConstructor(Array<String>::class.java)
                .newInstance(arrayOf(System.getProperty("cardr.data.dir")))
        )
        return Files.readString(Paths.get(System.getProperty("cardr.data.dir"), "ocr", "ocr-result.txt"))
    }

    private fun getOCRFromREST(imageFile: File): String {
        val client = HttpClientBuilder.create().build()
        val request = HttpPost("https://api.ocr.space/parse/image")
        request.setHeader("apikey", SecretData.OCRSPACE_APIKEY)
        request.entity = MultipartEntityBuilder.create()
            .addBinaryBody("file", imageFile)
            .build()

        val response: CloseableHttpResponse = client.execute(request)
        val data = response.entity.content.bufferedReader().use(BufferedReader::readText)
        val jsonData = JsonParser().parse(data).asJsonObject
        return StringEscapeUtils.unescapeJson(jsonData["ParsedResults"].asJsonArray[0].asJsonObject["ParsedText"].asString)
    }

    private fun loadOCRText(data: HashMap<String, Any>) {
        if (!data.containsKey("ocrText"))
            return

        var text = (data["ocrText"] as String).replace("\r","\n")
        while (text.contains("\n\n"))
            text = text.replace("\n\n","\n")
        text = text.trim()

        cardrUI.overrideBodyParagraphs = text.split("\n").toMutableList()
        val sb = StringBuilder()
        cardrUI.overrideBodyParagraphs!!.forEach {
            sb.append("<p>")

            sb.append(it)
            sb.append(' ')

            sb.append("</p>")
        }

        cardrUI.overrideBodyHTML = null
        cardrUI.enableCardBodyEditOptions()
        cardrUI.cardBody.set(sb.toString())
        cardrUI.refreshHTML()
    }

    private fun hideAllWindows() {
        cardrUI.stage.isIconified = true
        for (window in openWindows) {
            window.window.isIconified = true
        }
    }

    private fun showAllWindows() {
        cardrUI.stage.isIconified = false
        for (window in openWindows) {
            window.window.isIconified = false
        }
    }

    override fun generateUI(): Scene {
        menuBox.spacing = 10.0
        menuBox.padding = Insets(10.0)

        captureBtn.graphic = cardrUI.loadMiniIcon("/capture-ocr.png", true, 1.5)
        captureBtn.setOnAction { onCapture() }

        val closeBtn = Button("Close")
        closeBtn.graphic = cardrUI.loadMiniIcon("/close.png", true, 1.5)
        closeBtn.setOnAction {
            if (thread != null && thread!!.isAlive)
                thread!!.interrupt()
            close(null)
            showAllWindows()
        }

        menuBox.alignment = Pos.CENTER
        menuBox.children.addAll(captureBtn, closeBtn)

        val root = BorderPane()
        root.styleClass.add("custom-dashed-border")
        root.center = menuBox

        root.setOnMousePressed {
            if (it.sceneX < BORDER_SIZE || it.sceneY < BORDER_SIZE || abs(root.width - it.sceneX) < BORDER_SIZE || abs(root.height - it.sceneY) < BORDER_SIZE) {
                xOffset = -1.0
                yOffset = -1.0
            } else {
                xOffset = it.sceneX
                yOffset = it.sceneY
            }
        }

        root.setOnMouseDragged {
            if (xOffset == -1.0 && yOffset == -1.0)
                return@setOnMouseDragged
            window.x = it.screenX - xOffset
            window.y = it.screenY - yOffset
        }


        val scene = Scene(root, 400.0, 300.0)
        scene.fill = Color.web("#000000", 0.25)
        scene.stylesheets.add(javaClass.getResource("/styles-transparent.css").toExternalForm())
        window.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))
        return scene
    }

}