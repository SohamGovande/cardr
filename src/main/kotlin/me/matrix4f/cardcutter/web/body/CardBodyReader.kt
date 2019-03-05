package me.matrix4f.cardcutter.web.body

import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class CardBodyReader(private val hostName: String, private val doc: Document) {

    private fun businessinsider(): Elements {
        return  doc.select("div[data-piano-inline-content-wrapper] p")
    }

    private fun politico(): Elements {
        return doc.select(".story-text p")
    }

    private fun thinkprogress(): Elements {
        return doc.select(".post__content p")
    }

    private fun sciencemag(): Elements {
        return doc.select(".article__body p")
    }

    private fun nypost(): Elements {
        return doc.select(".entry-content p")
    }

    private fun delawareonline(): Elements {
        return doc.getElementsByClass(".p-text")
    }

    private fun aljazeera(): Elements {
        return doc.select(".article-p-wrapper p")
    }

    private fun wthr(): Elements {
        return doc.select(".field-items p")
    }

    private fun washingtonexaminer(): Elements {
        return doc.select(".RichTextArticleBody-body p")
    }

    private fun newyorker(): Elements {
        return doc.select(".SectionBreak p")
    }

    private fun vox(): Elements {
        return doc.select("" +
            "div.c-entry-content p, " +
            "div.c-entry-content blockquote, " +
            "div.c-entry-content p, " +
            "div.c-entry-content h3, " +
            "div.c-entry-content ul, " +
            "div.c-entry-content ol")
    }

    private fun cnn(): Elements { // Doesn't work - CNN waits to load the paragraphs
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

    private fun nytimes(): Elements {
        return doc.select(".StoryBodyCompanionColumn")
    }

    private fun foxnews(): Elements {
        return doc.select(".article-body p")
    }

    private fun theverge(): Elements {
        return doc.select(".c-entry-content p, .c-entry-content h2")
    }

    private fun wsj(): Elements {
        return doc.select(".wsj-snippet-body p")
    }

    private fun engadget(): Elements {
        return doc.select(".o-article_block p")
    }

    private fun bgr(): Elements {
        return doc.select(".entry-content p")
    }

    private fun aol(): Elements {
        return doc.select("#article-content p")
    }

    private fun dailywire(): Elements {
        return doc.select(".field-body p")
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