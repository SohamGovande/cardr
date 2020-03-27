package me.sohamgovande.cardr.core.web

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class SciHubLoader(doi: String) {

    var doc: Document
    val url = "http://sci-hub.tw/$doi"

    init {
        try {
            doc = Jsoup.connect(url).get()
        } catch (e: Exception) {
            e.printStackTrace()
            doc = Jsoup.parse("<html></html>")
        }

        val elements = doc.select("a[onclick]")
        for (element in elements) {
            val attrDownload = element.attr("onclick")
            if (attrDownload.contains("download")) {
                val downloadURL = attrDownload.substring("location.href='".length, attrDownload.length-"?download=true".length-1)
                val downloadName = downloadURL.substring(downloadURL.lastIndexOf('/')+1, downloadURL.length)
                // Download the pdf into a file
                val inputstream = URL(downloadURL).openStream()
                Files.copy(inputstream, Paths.get("pdfs",downloadName), StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}