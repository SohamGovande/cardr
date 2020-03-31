package me.sohamgovande.cardr.util

import org.apache.commons.exec.*
import org.apache.logging.log4j.Logger
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels


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
        logger.info("$cmd terminated with  exit $exitValue")
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
