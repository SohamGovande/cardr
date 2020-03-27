package me.sohamgovande.cardr.core.web

import org.apache.logging.log4j.LogManager
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

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

    private fun bloomberg(): Elements {
        return doc.select(".fence-body > p")
    }

    private fun businessinsider(): Elements {
        return doc.select("div[data-piano-inline-content-wrapper] p")
    }

    private fun cfr(): Elements {
        return Elements(doc.select(".body-content p").filter {
            !it.text().contains("This article first appeared ") &&
                !it.hasClass("more-on__title") &&
                !it.hasClass("more-on__content")
        })
    }

    private fun catoinstitute(): Elements {
        return doc.select("article p")
    }

    private fun cnn(): Elements {
        return doc.select(".body-text p")
    }

    private fun csgjusticecenter(): Elements {
        return doc.select(".wysiwyg p")
    }

    private fun dailywire(): Elements {
        return doc.select(".field-body p")
    }

    private fun delawareonline(): Elements {
        return doc.getElementsByClass(".p-text")
    }

    private fun eflux(): Elements {
        return doc.select(".block-text p")
    }

    private fun engadget(): Elements {
        return doc.select(".o-article_block p")
    }

    private fun foreignaffairs(): Elements {
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

    private fun huffpost(): Elements {
        return doc.select(".entry__text .text p")
    }

    private fun justicepolicy(): Elements {
        return Elements(doc.select("#content-primary p, #content-primary ul, #content-primary ol").filter {
            !it.hasClass("byline") && !it.hasClass("assigned-tags") && !it.text().startsWith("Keywords: ") && !it.text().startsWith("Posted in ")
        })
    }

    private fun mic(): Elements {
        return doc.select(".ykW p")
    }

    private fun motherjones(): Elements {
        return doc.select(".entry-content > p")
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

    private fun outline(): Elements {
        return doc.select("raw p")
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

    private fun prisonpolicy(): Elements {
        return Elements(doc.select("#page p").filter {
            !it.hasClass("attrib")
        })
    }

    private fun rand(): Elements {
        return Elements(doc.select(".product-main p, .product-main h2, .product-main h3, .product-main h4, .product-main ol, .product-main ul").filter {
            !it.parent().hasClass("conducted") && it.parent().id() != "indicia" && !it.hasClass("authors") && !it.hasClass("date") && !it.hasClass("type") && !it.parent().hasClass("text") && !it.parent().hasClass("section-inner")
        })
    }

    private fun reason(): Elements {
        return doc.select(".entry-content p")
    }

    private fun reuters(): Elements {
        val a = doc.select(".StandardArticleBody_body p").filter {
            !it.hasClass("Attribution_content")
        }
        return Elements(a)
    }

    private fun sciencedirect(): Elements {
        return doc.select(".Body p, .Body h2, .Body h3, .Body h4, .Body h5")
    }

    private fun sciencemag(): Elements {
        return doc.select(".article__body p")
    }

    private fun scmp(): Elements {
        return doc.select(".body .content--p")
    }

    private fun smithsonianmag(): Elements {
        return doc.select(".article-body > p")
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

    private fun thecrimereport(): Elements {
        return doc.select("div[itemprop=articleBody] > p")
    }

    private fun thediplomat(): Elements {
        return Elements(doc.select("#td-story-body p").filter {
            !it.attr("ng-show").contains("ad")
        })
    }

    private fun theeconomist(): Elements {
        return Elements(doc.select(".blog-post__text p").filter {
            !it.parent().hasClass("newsletter-form__message")
        })
    }

    private fun thefederalist(): Elements {
        return doc.select(".entry-content p, .entry-content h2, .entry-content h3")
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

    private fun usatoday(): Elements {
        return doc.select(".gnt_ar_b p")
    }

    private fun vera(): Elements {
        return doc.select(".module-text p")
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

    private fun washingtonexaminer(): Elements {
        return doc.select(".RichTextArticleBody-body p")
    }

    private fun washingtontimes(): Elements {
        return doc.select(".article-text p")
    }

    private fun worldpoliticsreview(): Elements {
        return doc.select("article p")
    }

    private fun wsj(): Elements {
        return Elements(doc.select(".wsj-snippet-body p, .article-content p").filter {
            !(it.text().contains("Copyright") && it.text().contains("Dow Jones & Company, Inc"))
        })
    }

    private fun wthr(): Elements {
        return doc.select(".field-items p")
    }

    fun getBodyParagraphs(fromCardID: Boolean): Elements {
        try {
            val hostName = hostName
                .replace(" ","")
                .replace(".","")
                .replace("-","")
                .replace(".org", "")
            logger.info("Loading article from host '$hostName'")

            if (doc.text().toLowerCase().contains("cardify error")) {
                return doc.select("p")
            }

            if (INCOMPATIBLE_SOURCES.contains(hostName) && !fromCardID) {
                return Jsoup.parse("<p>Unfortunately, publisher \"${hostName.toUpperCase()}\" did not allow cardr to view the article body. Please use Google Chrome and click the cardr icon for article access. If that doesn't work, please simply copy and paste the part of the article you wish into Word/Google Docs.</p>").body().children()
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
            "scmp",
            "bloomberg",
            "cnn",
            "wsj",
            "journals.sagepub",
            "outline",
            "usatoday"
        )

        val logger = LogManager.getLogger(CardBodyReader::class.java)
    }
}