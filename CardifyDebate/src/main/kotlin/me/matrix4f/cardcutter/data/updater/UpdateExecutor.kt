package me.matrix4f.cardcutter.data.updater

import me.matrix4f.cardcutter.CardifyDebate
import me.matrix4f.cardcutter.util.Hash
import me.matrix4f.cardcutter.util.downloadFileFromURL
import me.matrix4f.cardcutter.util.executeCommandBlocking
import me.matrix4f.cardcutter.util.executeCommandUnblocking
import org.apache.logging.log4j.LogManager
import java.nio.file.Paths
import kotlin.system.exitProcess

class UpdateExecutor(private val version: CardifyVersion) {

    var messageHandler = { _: String -> Unit }
    var onClose = { Unit }

    fun update() {
        logger.info("Checking Cardify updater...")
        messageHandler("Checking Cardify updater...")
        val cardifyUpdaterPath = Paths.get("app", "CardifyUpdater.jar")
        val cardifyUpdaterFile = cardifyUpdaterPath.toFile()
        cardifyUpdaterFile.parentFile.mkdir()

        var readSha256 = ""
        if (cardifyUpdaterFile.exists()) {
            readSha256 = Hash.SHA256.checksum(cardifyUpdaterFile)!!
        }

        val javaExe: String
        if (CardifyDebate.RELEASE_MODE) {
            javaExe = "\"runtime/bin/java.exe\""
        } else {
            javaExe = "java"
        }
        val cmd = "$javaExe -jar \"${cardifyUpdaterFile.canonicalPath}\""
        executeCommandBlocking(cmd, logger, allowNonzeroExit = true)
        exitProcess(0)
    }

    companion object {
        val logger = LogManager.getLogger(UpdateExecutor::class.java)
    }
}