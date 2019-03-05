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

    // Doesn't work - CNN waits to load the paragraphs
    private fun cnn(): Elements {
        return doc.getElementsByClass("zn-body__paragraph")
    }

    private fun bbc(): Elements {
        return doc.select(".story-body__inner p")
    }

    private fun thehill(): Elements {
        return doc.select(".field-name-body .field-items")
    }

    private fun reuters(): Elements {
        return doc.select(".StandardArticleBody_body p")
    }

    fun getBodyParagraphs(): Elements {
        try {
            return javaClass.getDeclaredMethod(hostName).invoke(this) as Elements
        } catch (e: Exception) {

            // NoSuchMethodException is normal, it means the host was unrecognized
            if (!(e is NoSuchMethodException))
                e.printStackTrace()

            /* if (e is NoSuchMethodException) */
            // Default behavior
            return doc.getElementsByTag("p")
        }
    }
}