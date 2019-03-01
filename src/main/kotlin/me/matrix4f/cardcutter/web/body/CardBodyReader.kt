package me.matrix4f.cardcutter.web.body

import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class CardBodyReader(private val hostName: String, private val doc: Document) {

    private fun vox(): Elements {
        return doc.select("" +
            "div.c-entry-content p, " +
            "div.c-entry-content blockquote, " +
            "div.c-entry-content p, " +
            "div.c-entry-content h3, " +
            "div.c-entry-content ul, " +
            "div.c-entry-content ol")
    }

    private fun cnn(): Elements {
        return doc.select(".zn-body__paragraph")
    }

    private fun reuters(): Elements {
        return doc.select("StandardArticleBody_body p")
    }

    fun getBodyParagraphs(): Elements {
        return when(hostName) {
            "cnn" -> cnn()
            "vox" -> vox()
            "reuters" -> reuters()
            else -> doc.getElementsByTag("p")
        }
    }
}