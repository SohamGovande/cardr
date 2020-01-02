package me.matrix4f.cardcutter.firstlaunch
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import me.matrix4f.cardcutter.util.OS
import me.matrix4f.cardcutter.util.getOSType
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.PumpStreamHandler
import java.awt.Desktop
import java.io.*
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.nio.channels.Channels

@Throws(FirstLaunchException::class, Exception::class)
private fun onFirstLaunchWindows() {

    val executable = File("CardifyChromeApp.exe")
    var dataStream = URL("http://cardifydebate.x10.bz/data/CardifyChromeAppWin.exe").openStream()
    var fos = FileOutputStream(executable)
    fos.channel.transferFrom(Channels.newChannel(dataStream), 0, Long.MAX_VALUE)
    dataStream.close()
    fos.close()

    val jsonFile = File("me.matrix4f.cardify.json")
    dataStream = URL("http://cardifydebate.x10.bz/data/ChromeAppNativeJsonWin.json").openStream()
    fos = FileOutputStream(jsonFile)
    fos.channel.transferFrom(Channels.newChannel(dataStream), 0, Long.MAX_VALUE)
    dataStream.close()
    fos.close()

    if (!jsonFile.exists())
        throw FirstLaunchException("Unable to download Chrome App Native JSON.")

    val commands = arrayOf(
        "REG DELETE \"HKCU\\Software\\Google\\Chrome\\NativeMessagingHosts\\me.matrix4f.cardify\" /f",
        "REG DELETE \"HKLM\\Software\\Google\\Chrome\\NativeMessagingHosts\\me.matrix4f.cardify\" /f",
        "REG ADD \"HKCU\\Software\\Google\\Chrome\\NativeMessagingHosts\\me.matrix4f.cardify\" /ve /t REG_SZ /d \"${jsonFile.absolutePath}\" /f"
    )
    for (cmd in commands) {
        val stdout = ByteArrayOutputStream()

        val stdoutPsh = PumpStreamHandler(stdout)
        val cmdLine = CommandLine.parse(cmd)
        val executor = DefaultExecutor()
        executor.streamHandler = stdoutPsh
        try {
            executor.execute(cmdLine)
        } catch (e: Exception) {
        }

//        val result = stdout.toString().replace("\n", " ")
//        parent.logMessage("Command '$cmd' returned $result")
    }
}

private fun onFirstLaunchMacOS() {
    val jsonFile = File("~/Library/Application Support/Google/Chrome/NativeMessagingHosts/me.matrix4f.cardify.json")

    val dataStream = URL("http://cardifydebate.x10.bz/data/ChromeAppNativeJsonMac.json").openStream()
    val fos = FileOutputStream(jsonFile)
    fos.channel.transferFrom(Channels.newChannel(dataStream), 0, Long.MAX_VALUE)
    dataStream.close()
    fos.close()

    if (!jsonFile.exists())
        throw FirstLaunchException("Unable to download Chrome App Native JSON.")
}

fun onFirstLaunch(): Exception? {
    try {
        if (getOSType() == OS.WINDOWS)
            onFirstLaunchWindows()
        else if (getOSType() == OS.MAC)
            onFirstLaunchMacOS()
        return null
    } catch (e: Exception) {
        return e
    }
}

fun showFirstLaunchError(e: Exception) {
    val sendButtonType = ButtonType("Send Error Output", ButtonBar.ButtonData.OK_DONE)
    val closeButtonType = ButtonType("Close Without Reporting", ButtonBar.ButtonData.CANCEL_CLOSE)

    val alert = Alert(Alert.AlertType.ERROR, "", closeButtonType, sendButtonType)
    alert.headerText = "Uh-oh! There was an error."
    alert.contentText = "We encountered an unknown error of type ${e.javaClass.simpleName}: ${e.message}. We apologize for the inconvenience. Please kindly select \"Send Error Output\" to share the error log with the developers so that they can fix the bug or help you out. We apologize for the inconvenience."
    val result = alert.showAndWait()
    if (result.orElse(closeButtonType) == sendButtonType) {
        val baos = ByteArrayOutputStream()
        val writer = PrintWriter(baos)
        e.printStackTrace(writer)
        writer.close()

        sendLog(baos.toString())
    }
    System.exit(0)
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
        "subject=${urlEncode("Cardify Windows Installer Error")}" +
        "&body=${urlEncode(msg)}"
    val uri = URI.create(message)
    desktop.mail(uri)
}
