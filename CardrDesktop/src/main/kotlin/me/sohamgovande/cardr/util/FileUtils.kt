package me.sohamgovande.cardr.util

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.exec.*
import org.apache.logging.log4j.Logger
import java.io.*
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Files
import java.util.*


fun makeFileExecutableViaChmod(path: String, logger: Logger) {
    executeCommandBlocking("chmod +x \"$path\"", logger, true)
}

@Throws(Exception::class)
fun downloadFileFromURL(url: String, downloadTo: File, logger: Logger) {
    logger.info("Opening URL $url")
    val dataStream = URL(url).openStream()
    val fos = FileOutputStream(downloadTo)
    logger.info("Transferring data from url $url to ${downloadTo.absolutePath}")
    fos.channel.transferFrom(Channels.newChannel(dataStream), 0, Long.MAX_VALUE)
    logger.info("Closing data streams")
    dataStream.close()
    fos.close()
}

@Throws(Exception::class)
fun executeCommandBlocking(cmd: String, logger: Logger, allowNonzeroExit: Boolean): String {
    logger.info("Running command '$cmd' (blocking)")
    val stdout = ByteArrayOutputStream()
    val stdoutPsh = PumpStreamHandler(stdout)
    val cmdLine = CommandLine.parse(cmd)
    val executor = DefaultExecutor()
    executor.streamHandler = stdoutPsh
    try {
        val exitValue = executor.execute(cmdLine)
        logger.info("$cmd terminated with exit $exitValue")
    } catch (e: ExecuteException) {
        if (e.message!!.contains("Process exited with an error:") && allowNonzeroExit) {
            logger.info("Error executing command $cmd but ignored return value '${e.message}'")
        } else {
            logger.error("Error executing command $cmd", e)
            throw e
        }
    }
    val result = stdout.toString()
    logger.info("Command '$cmd' returned '$result'")
    return result
}

@Throws(Exception::class)
fun executeCommandUnblocking(cmd: String, logger: Logger) {
    logger.info("Running command '$cmd' (unblocking)")
    val stdout = ByteArrayOutputStream()
    val stdoutPsh = PumpStreamHandler(stdout)
    val cmdLine = CommandLine.parse(cmd)
    val executor = DefaultExecutor()

    val resultHandler = DefaultExecuteResultHandler()
    executor.streamHandler = stdoutPsh

    try {
        val exitValue = executor.execute(cmdLine, resultHandler)
        logger.info("$cmd terminated with  exit $exitValue")
    } catch (e: Exception) {
        logger.info("Error executing command $cmd", e)
    }
}

fun extractZipFile(zipFileRaw: File, logger: Logger, destFolderRaw: String? = null, deleteOnExit: Boolean = true) {
    val buffer = 1024

    val zipFile = ZipFile(zipFileRaw)
    val data = ByteArray(buffer)
    val destFolder = destFolderRaw ?: (zipFileRaw.parent + File.separator)
    logger.info("Extracting ${zipFileRaw.absolutePath} to $destFolder")

    val entries: Enumeration<ZipArchiveEntry> = zipFile.entries

    while (entries.hasMoreElements()) {
        val zipEntry = entries.nextElement()
        val destFile = File(destFolder + zipEntry.name)

        if (zipEntry.isDirectory)
            destFile.mkdirs()
        else
            destFile.parentFile.mkdirs()

        if (zipEntry.isUnixSymlink) {
            if (getOSType() != OS.MAC) {
                logger.error("Error trying to unzip $zipFileRaw - encountered symlink ${zipEntry.name} on non-Mac computer")
            } else {
                val target = File(zipFile.getUnixSymlink(zipEntry))
                try {
                    val destPath = destFile.toPath()
                    Files.deleteIfExists(destPath)
                    Files.createSymbolicLink(destPath, target.toPath())
                    continue
                } catch (e: Exception) {
                    logger.error("Failed to create symbolic link: " +
                        destFile.absolutePath + " -> " +
                        target.absolutePath, e)
                }
            }
        }

        if (!destFile.isDirectory) {
            var count: Int
            val fos = FileOutputStream(destFile)
            BufferedOutputStream(fos, buffer).use { dest ->
                val stream = zipFile.getInputStream(zipEntry)
                while (stream.read(data, 0, buffer).also { count = it } != -1) {
                    dest.write(data, 0, count)
                }
            }
        }
    }

    if (deleteOnExit)
        zipFileRaw.deleteOnExit()
    zipFile.close()

    logger.info("... Finished extraction.")
}
