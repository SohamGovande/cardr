package me.sohamgovande.cardr.util

import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import java.awt.Desktop
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URLEncoder
import kotlin.system.exitProcess

fun showErrorDialog(brief: String, full: String) {
    Platform.runLater {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Error"
        alert.headerText = brief
        alert.contentText = full
        alert.showAndWait()
    }
}

fun showInfoDialogUnblocking(brief: String, full: String) {
    Platform.runLater {
        showInfoDialogBlocking(brief, full)
    }
}

fun showInfoDialogBlocking(brief: String, full: String) {
    val alert = Alert(Alert.AlertType.INFORMATION)
    alert.title = "Message"
    alert.headerText = brief
    alert.contentText = full
    alert.showAndWait()
}

fun showInfoDialogBlocking(brief: String, full: String, primaryOption: String, action: () -> Unit) {
    val primaryBT = ButtonType(primaryOption, ButtonBar.ButtonData.OK_DONE)
    val exitBT = ButtonType("OK", ButtonBar.ButtonData.CANCEL_CLOSE)

    val alert = Alert(Alert.AlertType.INFORMATION, "", primaryBT, exitBT)

    alert.title = "Message"
    alert.headerText = brief
    alert.contentText = full
    val result = alert.showAndWait()
    if (result.isPresent && result.get() == primaryBT) {
        action()
    }
}

fun showInfoDialogUnblocking(brief: String, full: String, primaryOption: String, action: () -> Unit) {
    Platform.runLater {
        showInfoDialogBlocking(brief, full, primaryOption, action)
    }
}

private fun urlEncode(str: String): String {
    return try {
        URLEncoder.encode(str, "UTF-8").replace("+", "%20")
    } catch (e: UnsupportedEncodingException) {
        throw RuntimeException(e)
    }
}

private fun sendLog(msg: String) {
    val desktop = Desktop.getDesktop()
    val message = "mailto:sohamthedeveloper@gmail.com?" +
        "subject=${urlEncode("Cardr Error")}" +
        "&body=${urlEncode(msg)}"
    val uri = URI.create(message)
    desktop.mail(uri)
}

fun showErrorDialog(e: Exception) {
    val sendBT = ButtonType("Send Log File", ButtonBar.ButtonData.OK_DONE)
    val continueBT = ButtonType("Continue", ButtonBar.ButtonData.OK_DONE)
    val exitBT = ButtonType("Exit", ButtonBar.ButtonData.CANCEL_CLOSE)

    val alert = Alert(Alert.AlertType.ERROR, "", sendBT, continueBT, exitBT)
    alert.headerText = "Uh-oh! There was an error."
    alert.contentText = "We encountered an unknown error of type ${e.javaClass.simpleName}: ${e.message}. We apologize for the inconvenience. Please kindly select \"Send Log File\" to share the error log with the developers so that they can fix the bug or help you out. We apologize for the inconvenience."
    val result = alert.showAndWait()
    var forceClose = true
    if (result.isPresent && result.get() == sendBT) {
        val baos = ByteArrayOutputStream()
        val writer = PrintWriter(baos)
        e.printStackTrace(writer)
        writer.close()

        sendLog(baos.toString())
    }
    if (result.isPresent && result.get() == continueBT) {
        forceClose = false
    }
    if (forceClose) {
        exitProcess(0)
    }
}
