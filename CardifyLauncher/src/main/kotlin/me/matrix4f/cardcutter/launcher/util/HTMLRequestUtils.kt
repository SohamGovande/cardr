package me.matrix4f.cardcutter.launcher.util

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import java.io.BufferedReader
import java.io.IOException


@Throws(IOException::class)
fun makeRequest(url: String): String? {
    val client = HttpClientBuilder.create()
        .build()
    val request = HttpGet(url)
    request.setHeader("User-Agent", "Java client")

    val response = client.execute(request)

    return response.entity.content.bufferedReader().use(BufferedReader::readText)
}