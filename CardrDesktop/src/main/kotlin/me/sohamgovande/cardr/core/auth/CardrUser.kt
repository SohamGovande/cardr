package me.sohamgovande.cardr.core.auth


import me.sohamgovande.cardr.util.makeCardrRequest
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.data.encryption.EncryptionHelper
import org.apache.http.message.BasicNameValuePair
import org.apache.logging.log4j.LogManager
import java.io.*

class CardrUser {

    fun visitWebsite(url: String) {
        makeCardrRequest("upd_history", mutableListOf(
            BasicNameValuePair("email", Prefs.get().emailAddress),
            BasicNameValuePair("token", Prefs.get().accessToken),
            BasicNameValuePair("url", url)
        ))
        // Ignore the result (for now)
    }

    fun renew(): CardrResult {
        return makeCardrRequest("renew", mutableListOf(
            BasicNameValuePair("email", Prefs.get().emailAddress),
            BasicNameValuePair("token", Prefs.get().accessToken)
        ))
    }

    fun login(email: String, password: String): CardrResult {
        try {
            val result = makeCardrRequest("login", mutableListOf(
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
            return CardrResult("login","error",e.javaClass.name,baos.toString("UTF-8"))
        }
    }

    companion object {
        val logger = LogManager.getLogger(CardrUser::class.java)
    }
}