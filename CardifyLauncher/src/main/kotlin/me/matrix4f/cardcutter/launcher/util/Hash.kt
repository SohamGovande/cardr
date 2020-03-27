package me.matrix4f.cardcutter.launcher.util

import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.experimental.and

private val logger = LogManager.getLogger("me.matrix4f.cardcutter.launcher.util.Hash")

private fun encodeHex(digest: ByteArray): String {
    val sb = StringBuilder()
    for (i in digest.indices) {
        sb.append(Integer.toString((digest[i] and 0xff.toByte()) + 0x100, 16).substring(1))
    }
    return sb.toString()
}

fun sha256File(path: Path): String {
    try {
        val md = MessageDigest.getInstance("SHA-256")
        val buffer = Files.readAllBytes(path)
        md.update(buffer)
        val digest = md.digest()
        return encodeHex(digest)
    } catch (e: Exception) {
        logger.error("Unable to get SHA256 of ${path.toFile().canonicalPath}", e)
        return e.message ?: ""
    }
}
