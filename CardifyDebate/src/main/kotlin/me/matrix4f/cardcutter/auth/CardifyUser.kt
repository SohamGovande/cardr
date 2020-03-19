package me.matrix4f.cardcutter.auth


import me.matrix4f.cardcutter.util.makeCardifyRequest
import me.matrix4f.cardcutter.prefs.Prefs
import me.matrix4f.cardcutter.prefs.encryption.EncryptionHelper
import org.apache.http.message.BasicNameValuePair
import org.apache.logging.log4j.LogManager
import java.io.*

class CardifyUser {

    fun visitWebsite(url: String) {
        makeCardifyRequest("upd_history", mutableListOf(
            BasicNameValuePair("email", Prefs.get().emailAddress),
            BasicNameValuePair("token", Prefs.get().accessToken),
            BasicNameValuePair("url", url)
        ))
        // Ignore the result (for now)
    }

    fun renew(): CardifyResult {
        return makeCardifyRequest("renew", mutableListOf(
            BasicNameValuePair("email", Prefs.get().emailAddress),
            BasicNameValuePair("token", Prefs.get().accessToken)
        ))
    }

    fun login(email: String, password: String): CardifyResult {
        try {
            val result = makeCardifyRequest("login", mutableListOf(
                BasicNameValuePair("email", email),
                BasicNameValuePair("password", password)
            ))
            if (result.wasSuccessful()) {
                val extraInfo = result.getEmbeddedInfo()
                val token = extraInfo["token"]!!

                Prefs.get().accessToken = token
                Prefs.get().emailAddress = email
                Prefs.save()

                Thread {
                    val encryptor = EncryptionHelper(EncryptionHelper.getEncryptionInfo())
                    Prefs.get().encryptedPassword = encryptor.encrypt(password)
                    Prefs.save()
                }.start()
            }
            return result
        } catch (e: Exception) {
            logger.error("Login", e)
            val baos = ByteArrayOutputStream()
            e.printStackTrace(PrintWriter(baos))
            return CardifyResult("login","error",e.javaClass.name,baos.toString("UTF-8"))
        }
    }

    companion object {
        val logger = LogManager.getLogger(CardifyUser::class.java)
    }
}