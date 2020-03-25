package me.matrix4f.cardcutter.util

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.*

enum class Hash(val hashName: String) {
    MD5("MD5"), SHA1("SHA1"), SHA256("SHA-256"), SHA512("SHA-512");

    fun checksum(input: File): String? {
        try {
            FileInputStream(input).use { `in` ->
                val digest = MessageDigest.getInstance(hashName)
                val block = ByteArray(4096)
                var length: Int
                while (`in`.read(block).also { length = it } > 0) {
                    digest.update(block, 0, length)
                }
                val bytes = digest.digest()
                return Base64.getEncoder().encodeToString(bytes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

}