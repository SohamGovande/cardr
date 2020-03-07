package me.matrix4f.cardcutter.web.body

import org.apache.logging.log4j.LogManager
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.*

class CardBodyReader(private val hostName: String, private val doc: Document) {

    private val logger = LogManager.getLogger(javaClass)

    private fun aljazeera(): Elements {
        val a = doc.select(".article-p-wrapper p").filter {
            !it.text().contains("The views expressed in this article are")
        }
        return Elements(a)
    }

    private fun apnews(): Elements {
        return Elements(doc.select(".Article p").filter {
            !it.text().equals("___") &&
                !it.text().matches(Regex("Associated Press.+contributed to this report."))
        })
    }

    private fun aol(): Elements {
        return doc.select("#article-content p")
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

    private fun bgr(): Elements {
        return doc.select(".entry-content p")
    }

    private fun businessinsider(): Elements {
        return doc.select("div[data-piano-inline-content-wrapper] p")
    }

    private fun councilonforeignrelations(): Elements {
        return Elements(doc.select(".body-content p").filter {
            !it.text().contains("This article first appeared ") &&
                !it.hasClass("more-on__title") &&
                !it.hasClass("more-on__content")
        })
    }

    private fun catoinstitute(): Elements {
        return Elements(doc.select("article p"))
    }

    private fun dailywire(): Elements {
        return doc.select(".field-body p")
    }

    private fun delawareonline(): Elements {
        return doc.getElementsByClass(".p-text")
    }

    private fun engadget(): Elements {
        return doc.select(".o-article_block p")
    }

    private fun foreignaffairsmagazine(): Elements {
        return Elements(doc.select(".article-dropcap-body p").filter {
            !it.hasClass("load-text")
        })
    }

    private fun foreignpolicy(): Elements {
        return doc.select(".post-content-main p")
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

    private fun hoover(): Elements {
        return doc.select(".field-name-body")
    }

    private fun nationalinterest(): Elements {
        return Elements(doc.select(".detail__content p").filter {
            (it.classNames().size == 0 || it.hasClass("flfc"))
                && !it.html().contains("<em>")
        })
    }

    private fun nbcnews(): Elements {
        return Elements(doc.select(".article-body__content p").filter {
            !it.text().contains("Download the NBC News app for full coverage and alerts on the latest news.") &&
                !it.text().contains("This site is protected by recaptcha") &&
                !it.html().contains("<em>")
        })
    }

    private fun newyorker(): Elements {
        return doc.select(".SectionBreak p")
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

    private fun npr(): Elements {
        return doc.select(".storytext p")
    }

    private fun nytimes(): Elements {
        return doc.select(".StoryBodyCompanionColumn p, .StoryBodyCompanionColumn h2, .StoryBodyCompanionColumn h3, .articleBody p")
    }

    private fun phys(): Elements {
        val a = doc.select(".article-main p").filter {
            !it.text().equals("Explore further")
        }
        return Elements(a)
    }

    private fun politico(): Elements {
        val a = doc.select(".story-text p").filter {
            val tagFilter = if (it.classNames().size == 0) {
                true
            } else {
                var retVal = false
                for (name in it.classNames()) {
                    if (name.contains("paragraph")) {
                        retVal = true
                    }
                }
                retVal
            }

            it.parent().tagName()!="figcaption"
                && !it.parent().hasClass("twitter-tweet")
                && tagFilter
                && !it.text().contains("Missing out on the latest scoops?")
                && !it.text().contains("A daily play-by-play of congressional news in your inbox.")
        }
        return Elements(a)
    }

    private fun reuters(): Elements {
        val a = doc.select(".StandardArticleBody_body p").filter {
            !it.hasClass("Attribution_content")
        }
        return Elements(a)
    }

    private fun sciencemag(): Elements {
        return doc.select(".article__body p")
    }

    private fun tandfonline(): Elements {
        return doc.select("article p, article h1, article h2, article h3, article h4")
    }

    private fun theatlantic(): Elements {
        val a = doc.select(".l-article__container p").filter {
            !it.text().startsWith("Updated on") &&
                !it.hasClass("c-recirculation-link") &&
                !it.hasClass("c-letters-cta__text")
        }
        return Elements(a)
    }

    private fun theconversation(): Elements {
        return doc.select("div[itemprop=articleBody] p")
    }

    private fun theeconomist(): Elements {
        return Elements(doc.select(".blog-post__text p").filter {
            !it.parent().hasClass("newsletter-form__message")
        })
    }

    private fun thediplomat(): Elements {
        return Elements(doc.select("#td-story-body p").filter {
            !it.attr("ng-show").contains("ad")
        })
    }

    private fun theguardian(): Elements {
        return Elements(doc.select(".content__article-body p"))
    }

    private fun thehill(): Elements {
        return doc.select("p")
    }

    private fun theintercept(): Elements {
        return doc.select(".PostContent p")
    }

    private fun thewashingtonpost() : Elements {
        val a = doc.select("article p").filter {
            !it.hasClass("interstitial-link ") &&
                !it.hasClass("trailer")
        }
        return Elements(a)
    }

    private fun theverge(): Elements {
        return doc.select(".c-entry-content p, .c-entry-content h2")
    }

    private fun thinkprogress(): Elements {
        return doc.select(".post__content p")
    }

    private fun washingtonexaminer(): Elements {
        return doc.select(".RichTextArticleBody-body p")
    }

    private fun worldpoliticsreview(): Elements {
        return doc.select("article p")
    }

    private fun wthr(): Elements {
        return doc.select(".field-items p")
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

    fun getBodyParagraphs(): Elements {
        try {
            val hostName = hostName
                .replace(" ","")
                .replace(".org", "")
            logger.info("Loading article from publisher '$hostName'")

            if (INCOMPATIBLE_SOURCES.contains(hostName)) {
                return Jsoup.parse("<p>Unfortunately, publisher \"${hostName.toUpperCase()}\" did not allow Cardify to view the article body. Please refer to the online version for article access.</p>").body().children()
            } else {
                if (hostName.contains("bbc"))
                    return bbc()
                return javaClass.getDeclaredMethod(hostName)
                    .invoke(this) as Elements
            }
        } catch (e: Exception) {

            // NoSuchMethodException is normal, it means the host was unrecognized
            if (!(e is NoSuchMethodException)) {
                logger.error("Error reading card body", e)
            }

            /* if (e is NoSuchMethodException) */
            // Default behavior
            return doc.getElementsByTag("p")
        }
    }

    companion object {
        val INCOMPATIBLE_SOURCES = arrayOf(
            "southchinamorningpost",
            "bloomberg",
            "cnn",
            "wsj",
            "wallstreetjournal",
            "journals.sagepub",
            "usatoday"
        )

        val logger = LogManager.getLogger(CardBodyReader::class.java)
    }
}