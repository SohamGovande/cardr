package me.matrix4f.cardcutter.util

import com.google.gson.GsonBuilder
import me.matrix4f.cardcutter.core.auth.CardifyResult
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.logging.log4j.LogManager
import java.io.BufferedReader
import java.io.IOException

private val logger = LogManager.getLogger("me.matrix4f.cardcutter.util.HTMLRequestUtils")

fun makeCardifyRequest(function: String, params: MutableList<BasicNameValuePair>): CardifyResult {
    params.add(BasicNameValuePair("function", function))
    val resultData = makePOSTRequest(
        "http://www.cardifydebate.x10.bz/process.php",
        params
    )
    if (resultData == null) {
        return CardifyResult(
            function,
            "Error",
            "Couldn't get a response from the server.",
            ""
        )
    }
    val gson = GsonBuilder().setPrettyPrinting().setLenient().create()
    val readObject: CardifyResult
    try {
        readObject = gson.fromJson(resultData, CardifyResult::class.java)
    } catch (e: Exception) {
        return CardifyResult(
            function,
            "Error",
            "Couldn't parse server's response: '$resultData'.",
            ""
        )
    }
    return readObject
}

fun makePOSTRequest(url: String, params: List<NameValuePair>): String? {
    try {
        val client = HttpClientBuilder.create()
            .build()
        val request = HttpPost(url)
        request.setHeader("User-Agent", "Java client")
        request.entity = UrlEncodedFormEntity(params)

        val response = client.execute(request)

        return response.entity.content.bufferedReader().use(BufferedReader::readText)
    } catch (e: IOException) {
        logger.error("Error in local IO making HTTP POST request", e)
        return """
            {"func":"local_io","status":"error","reason":"${e.javaClass.simpleName} due to ${e.message}","additional_info":"Read log file"}
        """.trimIndent()
    }
}


@Throws(IOException::class)
fun makeRequest(url: String): String? {
    val client = HttpClientBuilder.create()
        .build()
    val request = HttpGet(url)
    request.setHeader("User-Agent", "Java client")

    val response = client.execute(request)

    return response.entity.content.bufferedReader().use(BufferedReader::readText)
}