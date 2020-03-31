package me.sohamgovande.cardr.core.auth


import me.sohamgovande.cardr.data.encryption.EncryptionHelper
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.util.makeCardrRequest
import org.apache.http.message.BasicNameValuePair
import org.apache.logging.log4j.LogManager
import java.io.ByteArrayOutputStream
import java.io.PrintWriter

class CardrUser {

    var onSuccessfulLogin = { -> Unit }

    fun visitWebsite(url: String) {
        makeCardrRequest("upd_history", mutableListOf(
            BasicNameValuePair("email", Prefs.get().emailAddress),
            BasicNameValuePair("token", Prefs.get().accessToken),
            BasicNameValuePair("url", url)
        ))
        // Ignore the result (for now)
    }

    fun renew(): CardrResult {
        val result = makeCardrRequest("renew", mutableListOf(
            BasicNameValuePair("email", Prefs.get().emailAddress),
            BasicNameValuePair("token", Prefs.get().accessToken)
        ))
        if (result.wasSuccessful())
            onSuccessfulLogin()
        return result
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
                onSuccessfulLogin()

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