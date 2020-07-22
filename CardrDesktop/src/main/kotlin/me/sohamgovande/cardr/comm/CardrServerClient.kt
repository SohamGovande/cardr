package me.sohamgovande.cardr.comm

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import javafx.application.Platform
import me.sohamgovande.cardr.core.ui.tabs.EditCardTabUI
import me.sohamgovande.cardr.processArgs
import me.sohamgovande.cardr.ui
import org.apache.logging.log4j.LogManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket

object CardrServerClient {

    private lateinit var serverSocket: ServerSocket
    private lateinit var outWriter: PrintWriter
    private lateinit var inReader: BufferedReader

    val logger = LogManager.getLogger(CardrServerClient::class.java)
    const val PORT = 25129

    fun checkExistingServers(args: List<String>): Boolean {
        val socket: Socket
        try {
            socket = Socket("localhost", PORT)
        } catch (e: Exception) {
            logger.info("No servers found")
            return false
        }

        try {
            val cOutWriter = PrintWriter(socket.getOutputStream(), true)
            val cInReader = BufferedReader(InputStreamReader(socket.getInputStream()))

            var line = cInReader.readLine()
            if (line == "cardr-server") {
                val jsonArray = JsonArray()
                for (arg in args)
                    jsonArray.add(arg)
                cOutWriter.println(GsonBuilder().create().toJson(jsonArray))
                line = cInReader.readLine()
                if (line == "received") {
                    logger.info("Successfully sent data to server")
                    logger.info("Closing cardr...")
                    socket.close()
                    return true
                } else {
                    logger.info("Unexpected error - received \"$line\", expected \"received\"")
                }
            } else {
                logger.info("Unexpected error - received \"$line\", expected \"cardr-server\"")
            }
            socket.close()
        } catch (e: Exception) {
            logger.info("Error checking existing servers", e)
        }
        return false
    }

    fun createNewServer() {
        try {
            serverSocket = ServerSocket(PORT, 1)
        } catch (e: Exception) {
            logger.error("Error starting server", e)
            return
        }
        try {
            while (true) {
                val socket = serverSocket.accept()
                outWriter = PrintWriter(socket.getOutputStream(), true)
                inReader = BufferedReader(InputStreamReader(socket.getInputStream()))

                outWriter.println("cardr-server")
                val lineRaw = inReader.readLine()
                logger.info("Read data: $lineRaw")
                val jsonArray = JsonParser().parse(lineRaw).asJsonArray
                outWriter.println("received")

                val argsList = jsonArray.map { it.asString }.toList()
                Platform.runLater {
                    val cardrUI = ui!!
                    cardrUI.createNewEditTab(null)
                    val newTab = cardrUI.getSelectedTab(EditCardTabUI::class.java)!!

                    logger.info("Processing args: $argsList")
                    processArgs(argsList, { cardrUI }, { newTab })
                }
            }
        } catch (e: Exception) {
            logger.error("Error accepting clients", e)
        }
    }
}
