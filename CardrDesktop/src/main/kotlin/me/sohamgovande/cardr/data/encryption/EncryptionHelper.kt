package me.sohamgovande.cardr.data.encryption

import com.google.gson.GsonBuilder
import me.sohamgovande.cardr.data.urls.UrlHelper
import me.sohamgovande.cardr.util.makeRequest
import org.apache.commons.codec.binary.Base64
import org.apache.logging.log4j.LogManager
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class EncryptionHelper(private val info: EncryptionInfo) {

    @Throws(Exception::class)
    fun encrypt(input: String): String {
        val password = info.localPasswordStorageKey
        val ivBytes: ByteArray
        val random = SecureRandom()
        val bytes = ByteArray(20)
        random.nextBytes(bytes)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec = PBEKeySpec(password.toCharArray(), bytes, 65556, 256)
        val secretKey = factory.generateSecret(spec)
        val secret = SecretKeySpec(secretKey.encoded, "AES")
        //encrypting the word
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secret)
        val params = cipher.parameters
        ivBytes = params.getParameterSpec(IvParameterSpec::class.java).iv
        val encryptedTextBytes = cipher.doFinal(input.toByteArray(charset("UTF-8")))
        //prepend salt and vi
        val buffer = ByteArray(bytes.size + ivBytes.size + encryptedTextBytes.size)
        System.arraycopy(bytes, 0, buffer, 0, bytes.size)
        System.arraycopy(ivBytes, 0, buffer, bytes.size, ivBytes.size)
        System.arraycopy(encryptedTextBytes, 0, buffer, bytes.size + ivBytes.size, encryptedTextBytes.size)
        return Base64().encodeToString(buffer)
    }

    @Throws(Exception::class)
    fun decrypt(encryptedText: String): String {
        val password = info.localPasswordStorageKey
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val buffer: ByteBuffer = ByteBuffer.wrap(Base64().decode(encryptedText))
        val saltBytes = ByteArray(20)
        buffer.get(saltBytes, 0, saltBytes.size)
        val ivBytes1 = ByteArray(cipher.blockSize)
        buffer.get(ivBytes1, 0, ivBytes1.size)
        val encryptedTextBytes = ByteArray(buffer.capacity() - saltBytes.size - ivBytes1.size)
        buffer.get(encryptedTextBytes)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, 65556, 256)
        val secretKey = factory.generateSecret(spec)
        val secret = SecretKeySpec(secretKey.encoded, "AES")
        cipher.init(Cipher.DECRYPT_MODE, secret, IvParameterSpec(ivBytes1))
        var decryptedTextBytes: ByteArray? = null
        try {
            decryptedTextBytes = cipher.doFinal(encryptedTextBytes)
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        }
        return String(decryptedTextBytes!!)
    }


    companion object {
        val logger = LogManager.getLogger(EncryptionHelper::class.java)

        @JvmStatic
        fun getEncryptionInfo(): EncryptionInfo {
            try {
                return GsonBuilder().create().fromJson(makeRequest(UrlHelper.get("encryptionInfo")), EncryptionInfo::class.java)
            } catch (e: Exception) {
                logger.error("Unable to extract encryption key - attempting to use default", e)
                return EncryptionInfo("af8199fd84720b524f22f7aaa2a04ca75b372ffec3b01d6fa6de29a23bc97b8d")
            }
        }
    }
}