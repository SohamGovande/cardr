package me.matrix4f.cardcutter.util

import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecuteResultHandler
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.PumpStreamHandler
import org.apache.logging.log4j.Logger
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels


fun makeFileExecutableViaChmod(path: String, logger: Logger) {
    executeCommandBlocking("chmod +x \"$path\"", logger)
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


fun executeCommandBlocking(cmd: String, logger: Logger) {
    logger.info("Running command '$cmd' (blocking)")
    val stdout = ByteArrayOutputStream()
    val stdoutPsh = PumpStreamHandler(stdout)
    val cmdLine = CommandLine.parse(cmd)
    val executor = DefaultExecutor()
    executor.streamHandler = stdoutPsh
    try {
        val exitValue = executor.execute(cmdLine)
        logger.info("$cmd terminated with  exit $exitValue")
    } catch (e: Exception) {
        logger.info("Error executing command $cmd", e)
    }
    val result = stdout.toString().replace("\n", "")
    logger.info("Command '$cmd' returned '$result'")
}


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
