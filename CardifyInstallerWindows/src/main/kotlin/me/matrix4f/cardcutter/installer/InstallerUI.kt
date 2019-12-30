package me.matrix4f.cardcutter.installer

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.Stage
import java.awt.Desktop
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URLEncoder


class InstallerUI(private val stage: Stage) {

    val installBtn = Button("Install Cardify")
    val textArea = TextArea()

    fun logBegin(step: String) {
        Platform.runLater { textArea.text += "$step...\n" }
    }

    fun logMessage(msg: String) {
        Platform.runLater { textArea.text += "$msg\n" }
    }

    private fun onClickInstallBtn() {
        Platform.runLater {
            installBtn.isDisable = true
            installBtn.text = "Installing..."
            textArea.isDisable = false
            textArea.text = "INSTALLER OUTPUT\n"
        }

        try {
            val executor = Executor(this)
            executor.downloadZip()
            executor.regedit()
        } catch (e: CardifyInstallException) {
            logMessage("[ERROR] Uh-oh! We encountered an error: ${e.message}")
            logMessage("[ERROR] Please kindly click \"Send Error Output\" to e-mail this output log to the developers, or copy and paste this installer log as an e-mail to sohamthedeveloper@gmail.com.")
            logMessage("[ERROR] We apologize for the inconvenience.")
        } catch (e: Exception) {
            // Get the stack trace as a string
            val baos = ByteArrayOutputStream()
            val writer = PrintWriter(baos)
            e.printStackTrace(writer)
            writer.close()

            logMessage("[ERROR] STACK TRACE:")
            logMessage(baos.toString())

            Platform.runLater {
                installBtn.text = "Retry Install"
                installBtn.isDisable = false

                val sendButtonType = ButtonType("Send Error Output", ButtonBar.ButtonData.OK_DONE)
                val closeButtonType = ButtonType("Close Without Reporting", ButtonBar.ButtonData.CANCEL_CLOSE)

                val alert = Alert(Alert.AlertType.ERROR, "", closeButtonType, sendButtonType)
                alert.headerText = "Uh-oh! There was an error."
                alert.contentText = "We encountered an unknown error of type ${e.javaClass.simpleName}: ${e.message}. We apologize for the inconvenience. Please kindly select \"Send Error Output\" to share the error log with the developers so that they can fix the bug or help you out. We apologize for the inconvenience."
                val result = alert.showAndWait()
                if (result.orElse(closeButtonType) == sendButtonType) {
                    sendLog()
                }
            }

            return
        }

        logMessage("Successfully installed CardifyDebate!")
        Platform.runLater {
            installBtn.text = "Finished"
            stage.scene = PostInstallUI().initialize()
        }
    }

    private fun sendLog() {
        val desktop = Desktop.getDesktop()
        val message = "mailto:sohamthedeveloper@gmail.com?" +
                "subject=${urlEncode("Cardify Windows Installer Error")}" +
                "&body=${urlEncode(textArea.text)}"
        val uri = URI.create(message)
        desktop.mail(uri)
    }


    private fun urlEncode(str: String): String {
        return try {
            URLEncoder.encode(str, "UTF-8").replace("+", "%20")
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }
    }

    fun initialize(): VBox {
        val box = VBox()
            box.style = "-fx-background-color:#fafafa;"
        box.spacing = 5.0
        box.padding = Insets(10.0)

        val header = Label("Welcome to the Cardify installer!")
        header.font = Font.font(20.0)
        val subheader = Label("Click \"Install Cardify\" below when you're ready to begin.")

        val buttonBox = HBox()
        buttonBox.spacing = 5.0
        installBtn.setOnAction {
            subheader.text = ""
            Thread { onClickInstallBtn() }.start()
        }
        buttonBox.children.add(installBtn)

        textArea.prefColumnCount = 60
        textArea.prefRowCount = 9
        textArea.isDisable = true
        textArea.isEditable = false

        box.children.add(header)
        box.children.add(subheader)
        box.children.add(textArea)
        box.children.add(buttonBox)
        return box
    }
}