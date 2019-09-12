package me.matrix4f.cardcutter.web.body

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.*

/**
 * Tailored to:
 * - POLITICO
 * - Reuters
 * - NY Post
 * - Al Jazeera
 * - Vox
 * - Washington Post
 * - Fox News
 * - Phys
 * - NY Times
 * - The Atlantic
 * - BBC
 */
class CardBodyReader(private val hostName: String, private val doc: Document) {

    private fun businessinsider(): Elements {
        return doc.select("div[data-piano-inline-content-wrapper] p")
    }

    private fun politico(): Elements {
        val a = doc.select(".story-text p").filter {
            it.parent().tagName()!="figcaption"
                && it.classNames().size == 0
                && !it.text().contains("Missing out on the latest scoops?")
                && !it.text().contains("A daily play-by-play of congressional news in your inbox.")
        }
        return Elements(a)
    }

    private fun thinkprogress(): Elements {
        return doc.select(".post__content p")
    }

    private fun sciencemag(): Elements {
        return doc.select(".article__body p")
    }

    private fun newyorkpost(): Elements {
        val a = doc.select(".entry-content p").filter {
            !it.parent().hasClass("thankyou") &&
                !it.parent().id().equals("footer-legal") &&
                !it.hasClass("byline") &&
                !it.hasClass("byline-date") &&
                !it.hasClass("read-next") &&
                !it.hasClass("read-next-link") &&
                !it.hasClass("share-count")
        }
        return Elements(a)
    }

    private fun theatlantic(): Elements {
        val a = doc.select(".l-article__container p").filter {
            !it.text().startsWith("Updated on") &&
                !it.hasClass("c-recirculation-link") &&
                !it.hasClass("c-letters-cta__text")
        }
        return Elements(a)
    }

    private fun delawareonline(): Elements {
        return doc.getElementsByClass(".p-text")
    }

    private fun aljazeera(): Elements {
        val a = doc.select(".article-p-wrapper p").filter {
            !it.text().contains("The views expressed in this article are")
        }
        return Elements(a)
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
        val a = doc.select("" +
            "div.c-entry-content p, " +
            "div.c-entry-content blockquote, " +
            "div.c-entry-content p, " +
            "div.c-entry-content h3, " +
            "div.c-entry-content ul, " +
            "div.c-entry-content ol")
        return Elements(a)
    }

    private fun councilonforeignrelations(): Elements {
        val a = doc.select(".body-content p").filter {
            !it.text().contains("This article first appeared ") &&
                !it.hasClass("more-on__title") &&
                !it.hasClass("more-on__content")
        }
        return Elements(a)
    }

    private fun cnn(): Elements { // Doesn't work - CNN waits to load the paragraphs
        return doc.getElementsByClass("zn-body__paragraph")
    }

    private fun bbc(): Elements {
        val a = doc.select("p").filter {
            !it.hasClass("twite__channel-text") &&
                !it.hasClass("twite__copy-text") &&
                !it.hasClass("twite__read-more") &&
                !it.hasClass("twite__new-window") &&
                !it.hasClass("twite__title") &&
                !it.hasClass("heron__item-summary")
        }
        return Elements(a)
    }

    private fun phys(): Elements {
        val a = doc.select(".article-main p").filter {
            !it.text().equals("Explore further")
        }
        return Elements(a)
    }

    private fun thehill(): Elements {
        return doc.select(".field-name-body .field-items")
    }

    private fun reuters(): Elements {
        val a = doc.select(".StandardArticleBody_body p").filter {
            !it.hasClass("Attribution_content")
        }
        return Elements(a)
    }

    private fun nytimes(): Elements {
        return doc.select(".StoryBodyCompanionColumn")
    }

    private fun foxnews(): Elements {
        val a = doc.select(".article-body p").filter {
            if (it.children().size > 0) {
                if (it.child(0).tagName().equals("strong"))
                    return@filter false
            }
            return@filter true
        }
        return Elements(a)
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

    private fun thewashingtonpost() : Elements {
        val a = doc.select("article p").filter {
            !it.hasClass("interstitial-link ") &&
                !it.hasClass("trailer")
        }
        return Elements(a)
    }

    fun getBodyParagraphs(): Elements {
        try {
            println(hostName)
            if (hostName.contains("bbc"))
                return bbc()
            return javaClass.getDeclaredMethod(hostName
                .replace(" ","")
                .replace(".org", ""))
                .invoke(this) as Elements
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