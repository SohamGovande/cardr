package me.matrix4f.cardcutter.auth


import me.matrix4f.cardcutter.util.makeCardifyRequest
import me.matrix4f.cardcutter.prefs.Prefs
import jdk.nashorn.internal.runtime.ScriptingFunctions.readLine
import org.apache.http.message.BasicNameValuePair
import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class CardifyUser {

    fun visitWebsite(url: String) {
        val result = makeCardifyRequest("upd_history", mutableListOf(
            BasicNameValuePair("email", Prefs.get().emailAddress),
            BasicNameValuePair("token", Prefs.get().accessToken),
            BasicNameValuePair("url", url)
        ))
        println(result)
        // Ignore the result (for now)
    }

    fun renew(): CardifyResult {
        return makeCardifyRequest("renew", mutableListOf(
            BasicNameValuePair("email", Prefs.get().emailAddress),
            BasicNameValuePair("token", Prefs.get().accessToken)
        ))
    }

    fun login(email: String, password: String): CardifyResult {
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
        }
        return result
    }
}