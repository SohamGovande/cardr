package me.matrix4f.cardcutter.installer

import net.lingala.zip4j.ZipFile
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.PumpStreamHandler
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Paths

class Executor(private val parent: InstallerUI) {

    private val mainFolder = Paths.get(System.getProperty("user.home"), "CardifyDebate")

    @Throws(CardifyInstallException::class, Exception::class)
    fun deleteFolder(folder: File) {
        val files = folder.listFiles()
        if (files != null) { //some JVMs return null for empty dirs
            for (f in files) {
                if (f.isDirectory) {
                    deleteFolder(f)
                } else {
                    f.delete()
                }
            }
        }
        folder.delete()
    }


    @Throws(CardifyInstallException::class, Exception::class)
    fun downloadZip() {
        val mainFolderFile = File(mainFolder.toUri())
        if (mainFolderFile.exists()) {
            parent.logBegin("Found previous CardifyDebate folder - deleting this")
            deleteFolder(mainFolderFile)
        }
        parent.logBegin("Creating new folder for 'CardifyDebate' at ${mainFolderFile.absolutePath}")
        if (!mainFolderFile.mkdir() && !mainFolderFile.exists()) {
            throw CardifyInstallException("Unable to create folder 'CardifyDebate' in home directory.")
        }

        val tempFile = File("cardify-temp-2ead02ec.zip")
        parent.logBegin("Creating URL to zipped data")
        val dataStream = URL("http://cardifydebate.x10.bz/data/Native_Win_Latest.zip").openStream()
        parent.logBegin("Downloading zipped data")
        val fos = FileOutputStream(tempFile)
        fos.channel.transferFrom(Channels.newChannel(dataStream), 0, Long.MAX_VALUE)
        dataStream.close()
        fos.close()

        parent.logBegin("Extracting zipped data")
        val zip = ZipFile(tempFile)
        zip.extractAll(mainFolder.normalize().toString())

        parent.logBegin("Deleting temporary file")
        tempFile.deleteOnExit()
    }

    @Throws(CardifyInstallException::class, Exception::class)
    fun regedit() {
        parent.logBegin("Editing registry")
        val commands = arrayOf(
            "REG DELETE \"HKCU\\Software\\Google\\Chrome\\NativeMessagingHosts\\me.matrix4f.cardify\" /f",
            "REG DELETE \"HKLM\\Software\\Google\\Chrome\\NativeMessagingHosts\\me.matrix4f.cardify\" /f",
            "REG ADD \"HKCU\\Software\\Google\\Chrome\\NativeMessagingHosts\\me.matrix4f.cardify\" /ve /t REG_SZ /d \"${mainFolder.toFile().absolutePath}\\me.matrix4f.cardify.json\" /f"
        )
        for (cmd in commands) {
            val stdout = ByteArrayOutputStream()

            val stdoutPsh = PumpStreamHandler(stdout)
            val cmdLine = CommandLine.parse(cmd)
            val executor = DefaultExecutor()
            executor.streamHandler = stdoutPsh
            try {
                executor.execute(cmdLine)
            } catch (e: Exception) {}

            val result = stdout.toString().replace("\n"," ")
            parent.logMessage("Command '$cmd' returned $result")
        }
    }
}