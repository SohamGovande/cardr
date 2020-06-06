package me.sohamgovande.cardr.core.web

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import javafx.beans.property.SimpleStringProperty
import me.sohamgovande.cardr.CardrDesktop
import me.sohamgovande.cardr.core.card.Author
import me.sohamgovande.cardr.core.card.Timestamp
import me.sohamgovande.cardr.core.ui.CardrUI
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.util.*
import org.apache.commons.text.StringEscapeUtils
import org.apache.logging.log4j.LogManager
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.BufferedReader
import java.net.URI
import java.nio.file.Paths
import java.security.SecureRandom
import javax.net.ssl.SSLContext


class WebsiteCardCutter(var cardrUI: CardrUI?, private val url: String, private val cardID: String?) {

    private val logger = LogManager.getLogger(javaClass)
    private lateinit var doc: Document
    private val meta: Elements
    private var metaJson: JsonObject

    // Stored information
    private var publisher: String? = null
    private var bodyParagraphElements: Elements? = null
    private var titleString: String? = null

    // Matchers
    private val authorMatcher = AuthorRegexMatcher()
    private val dateMatcher = DateRegexMatcher()

    init {
        try {
            if (cardID == null) {
                loadURL(url)
            } else {
                try {
                    val htmlDataFile = Paths.get(System.getProperty("cardr.data.dir"), "CardrPage-$cardID.html").toFile()
                    val htmlData: String = htmlDataFile.inputStream().bufferedReader().use(BufferedReader::readText)
                    doc = Jsoup.parse(htmlData)
                    if (CardrDesktop.RELEASE_MODE)
                        htmlDataFile.deleteOnExit()
                } catch (e: Exception) {
                    logger.error("Unable to load card ID $cardID", e)
                    loadURL(url)
                }
            }
        } catch (e: Exception) {
            doc = Jsoup.parse("<html></html>")
            logger.error("Unable to load URL: $url", e)
            showErrorDialog("Error loading URL: ${e.message}", "A ${e.javaClass.simpleName} exception occurred while loading $url")
        }
        meta = doc.getElementsByTag("meta")
        try {
            var jsonText = doc.select("script[type='application/ld+json']").maxBy { it.html().length }?.html() ?: throw NullPointerException()

            //Remove the "headline" and "description" attributes - if they contain quotes, they cause a JSON parse failure
            jsonText = jsonText.replace(Regex("\"headline\":\\s\".+\","), "")
                .replace(Regex("\"description\":\\s\".+\","), "")

            metaJson = JsonParser().parse(jsonText).asJsonObject
        } catch (e: Exception) {
            metaJson = JsonObject()
        }

        // REGISTER AUTHOR REGEXES
        authorMatcher.register("(?i)(By )([\\w.]+) ([\\w.]+)(?=, \\w+)") { AuthorList(Author(it[2]!!.value, it[3]!!.value)) }
        authorMatcher.register("(?i)(By )([\\w.]+)\\s([\\w.]+)\\s+(and|&)\\s+([\\w.]+)\\s([\\w.]+)") {
            AuthorList(arrayOf(
                Author(it[2]!!.value, it[3]!!.value),
                Author(it[5]!!.value, it[6]!!.value)
            ))
        }
        authorMatcher.register("(?i)(By )([\\w.]+)\\s(and|&)\\s([\\w.]+)") {
            AuthorList(arrayOf(
                Author("", it[2]!!.value),
                Author("", it[4]!!.value)
            ))
        }
        authorMatcher.register("(?i)(By )([\\w.]+),\\s([\\w.]+),\\s(and|&)\\s([\\w.]+)") {
            AuthorList(arrayOf(
                Author("", it[2]!!.value),
                Author("", it[3]!!.value),
                Author("", it[5]!!.value)
            ))
        }
        // Make sure this is the last one
        authorMatcher.register("(?i)(By )([\\w.]+) ([\\w.]+)") { AuthorList(Author(it[2]!!.value, it[3]!!.value)) }

        // REGISTER DATE REGEXES
        dateMatcher.register(RegexDatePattern(
            "(?i)(jan(uary)?|feb(ruary)?|mar(ch)?|apr(il)?|may|jun(e)?|jul(y)?|aug(ust)?|sep(t(ember)?)?|oct(ober)?|nov(ember)?|dec(ember)?)[,.]?\\s([0-9][0-9]?)(st|nd|rd|th)?[ ,.]+([0-9]+)?",
            { convertMonthNameToNumber(it[1]!!.value) },
            { it[14]!!.value.toInt().toString() },
            { ensureYYYYFormat(it[16]?.value ?: currentDate().year.toString()) }))

        dateMatcher.register(RegexDatePattern(
            "(?i)([0-9][0-9]?)(st|nd|rd|th)?\\s(jan(uary)?|feb(ruary)?|mar(ch)?|apr(il)?|may|jun(e)?|jul(y)?|aug(ust)?|sep(t(ember)?)?|oct(ober)?|nov(ember)?|dec(ember)?)[ ,.]+([0-9]+)?",
            { convertMonthNameToNumber(it[3]!!.value) },
            { it[1]!!.value.toInt().toString() },
            { ensureYYYYFormat(it[16]?.value ?: currentDate().year.toString()) }))

        dateMatcher.register(RegexDatePattern(
            "(?i)([0-9][0-9][0-9][0-9])[ ,.]+(jan(uary)?|feb(ruary)?|mar(ch)?|apr(il)?|may|jun(e)?|jul(y)?|aug(ust)?|sep(t(ember)?)?|oct(ober)?|nov(ember)?|dec(ember)?)[,.]?\\s([0-9][0-9]?)(st|nd|rd|th)?",
            { convertMonthNameToNumber(it[2]!!.value) },
            { it[15]!!.value.toInt().toString() },
            { ensureYYYYFormat(it[1]?.value ?: currentDate().year.toString()) }))

        dateMatcher.register(RegexDatePattern(
            "([0-9][0-9]?)[-./ ]([0-9][0-9]?)[-./ ]([0-9][0-9]([0-9][0-9])?)",
            { it[1]!!.value.toInt().toString() },
            { it[2]!!.value.toInt().toString() },
            { ensureYYYYFormat(it[3]?.value ?: currentDate().year.toString()) }))

        dateMatcher.register(RegexDatePattern(
            "(?i)(jan(uary)?|feb(ruary)?|mar(ch)?|apr(il)?|may|jun(e)?|jul(y)?|aug(ust)?|sep(t(ember)?)?|oct(ober)?|nov(ember)?|dec(ember)?)\\s+(\\d\\d\\d\\d)",
            { convertMonthNameToNumber(it[1]!!.value) },
            { "" },
            { ensureYYYYFormat(it[14]?.value ?: currentDate().year.toString()) }))
    }

    @Throws(Exception::class)
    private fun loadURL(url: String) {
        try {
            doc = Jsoup.connect(url).get()
            logger.info("Successfully loaded URL $url")
        } catch (e: Exception) {
            logger.info("Unable to load URL $url normally - trying to fall back on TLS v1.2")
            try {
                // Fallback on TLS v1.2
                val connection = Jsoup.connect(url)
                val sslContext = SSLContext.getInstance("TLSv1.2")
                sslContext.init(null, null, SecureRandom())
                val socketFactory = sslContext.socketFactory
                connection.sslSocketFactory(socketFactory)
                doc = connection.get()
            } catch (e: Exception) {
                logger.info("TLS 1.2 fallback failed - error loading URL")
                throw e
            }
        }
    }

    private fun findMeta(vararg attributes: String): String? {
        for (metaTag in meta) {
            for (attrib in attributes ) {
                if (metaTag.attr("name").equals(attrib, ignoreCase = true) ||
                        metaTag.attr("property").equals(attrib, ignoreCase = true) ||
                        metaTag.attr("itemprop").equals(attrib, ignoreCase = true)) {
                    return metaTag.attr("content")
                }
            }
        }
        return null
    }

    private fun getAuthorFromName(name: String): Author {
        if (name == "BBC News")
            return Author("", "BBC")
        else if (name == "Phys")
            return Author("", "Phys")

        val lastSpace = name.trim().lastIndexOf(' ')
        if (lastSpace == -1)
            return Author(SimpleStringProperty(""), SimpleStringProperty(name))

        val firstName = applyCorrectCapitalization(name.substring(0, lastSpace).trim())
        val lastName = applyCorrectCapitalization(name.substring(lastSpace+1).trim())
        return Author(firstName, lastName)
    }

    private fun getAuthorFromXML(): Array<Author>? {
        var authorStr: String = findMeta("author", "dcterms.creator", "og:article:author") ?: return null
        if (authorStr.contains("www"))
            return null
        val parsed = authorMatcher.evaluateString(authorStr)
        if (parsed != null) return parsed.value

        val firstPunctuation = Regex("[^a-zA-Z &.()*]").find(authorStr)?.range?.first ?: authorStr.length
        authorStr = authorStr.substring(0, firstPunctuation)

        return arrayOf(getAuthorFromName(authorStr))
    }

    private fun getPublicationSpecificAuthors(): Array<Author>? {
        if (getHostName(url) == "phys") {
            return arrayOf(getAuthorFromName("Phys"))
        } else if (getPublication() == "SAGE Journals") {
            return doc.select("[property='article:author']")
                .map { it.attr("content") }
                .map { it.split(", ") }
                .map { list ->
                    list.map { arrayOf(getAuthorFromName(it)) }
                        .flatMap { it.asIterable() }
                        .toList()
                }.flatMap { it.asIterable() }
                .distinct()
                .toTypedArray()
        } else if (getPublication() == "Taylor & Francis") {
            return doc.select("a.entryAuthor")
                .map { it.ownText().trim() }
                .map { authorMatcher.evaluateString(it)?.value ?: arrayOf(getAuthorFromName(it)) }
                .flatMap { it.asIterable() }
                .distinct()
                .toTypedArray()
        } else if (getPublication() == "The Diplomat") {
            return doc.select(".td-author strong, .td-author a[itemprop=author]")
                .map { authorMatcher.evaluateString("By " + it.text())?.value ?: arrayOf(getAuthorFromName(it.text())) }
                .flatMap { it.asIterable() }
                .distinct()
                .toTypedArray()
        } else if (getPublication() == "The Intercept" || getPublication() == "National Public Radio") {
            return doc.select("a[rel=author]")
                .filter { it.text().isNotBlank() }
                .map { authorMatcher.evaluateString("By " + it.text())?.value ?: arrayOf(getAuthorFromName(it.text())) }
                .flatMap { it.asIterable() }
                .distinct()
                .toTypedArray()
        } else if (getHostName(url) == "sciencedirect") {
            val a = doc.select(".author span.content")
            val list = arrayListOf<Author>()
            for (elem in a) {
                list.add(getAuthorFromName(elem.select(".text").joinToString(separator = " ") { it.text() }))
            }

            return list.toTypedArray()
        } else if (getHostName(url) == "mic") {
            return doc.select(".Neu")
                .filter { it.text().isNotBlank() }
                .map { authorMatcher.evaluateString("By " + it.text())?.value ?: arrayOf(getAuthorFromName(it.text())) }
                .flatMap { it.asIterable() }
                .distinct()
                .toTypedArray()
        } else if (getHostName(url) == "csgjusticecenter") {
            return doc.select(".article-byline__author")
                .map { authorMatcher.evaluateString(it.text())?.value ?: arrayOf(getAuthorFromName(it.text())) }
                .flatMap { it.asIterable() }
                .distinct()
                .toTypedArray()
        } else if (getHostName(url) == "prisonpolicy") {
            return doc.select(".attrib")
                .map { authorMatcher.evaluateString(it.text())?.value ?: arrayOf(getAuthorFromName(it.text())) }
                .flatMap { it.asIterable() }
                .distinct()
                .toTypedArray()
        } else if (getHostName(url) == "thecrimereport") {
            return doc.select(".by-author")
                .map { authorMatcher.evaluateString(it.text())?.value ?: arrayOf(getAuthorFromName(it.text())) }
                .first()
        } else if (getHostName(url) == "vera") {
            return doc.select(".post-content__author .person-name")
                .map { arrayOf(getAuthorFromName(it.text())) }
                .flatMap { it.asIterable() }
                .distinct()
                .toTypedArray()
        } else if (getHostName(url) == "justicepolicy") {
            return arrayOf(Author("", "Justice Policy Institute"))
        } else if (getHostName(url) == "thefederalist") {
            return doc.select(".rwd-byline")
                .map { authorMatcher.evaluateString(it.text())?.value ?: arrayOf(getAuthorFromName(it.text())) }
                .flatMap { it.asIterable() }
                .distinct()
                .toTypedArray()
        } else if (getHostName(url) == "e-flux") {
            return doc.select(".article-authors")
                .map { authorMatcher.evaluateString("By " + it.text())?.value ?: arrayOf(getAuthorFromName(it.text())) }
                .flatMap { it.asIterable() }
                .distinct()
                .toTypedArray()
        }
        return null
    }

    fun getAuthors(): Array<Author>? {
        val authors: Array<Author> = getRawAuthors() ?: return null
        for (author in authors) {
            unescapeHTML(author.firstName)
            unescapeHTML(author.lastName)
            unescapeHTML(author.qualifications)
        }
        return authors
    }

    fun getRawAuthors(): Array<Author>? {
        val publicationSpecificAuthor = getPublicationSpecificAuthors()
        if (publicationSpecificAuthor != null)
            return publicationSpecificAuthor

        var author: String? = null
        var authors: Array<Author>? = arrayOf()

        if (metaJson.has("author")) {
            val authorJson = metaJson["author"]
            if (authorJson.isJsonObject)
                author = authorJson.asJsonObject["name"].asString
            else if (authorJson.isJsonPrimitive && authorJson.asJsonPrimitive.isString)
                author = authorJson.asJsonPrimitive.asString
            else if (authorJson.isJsonArray) {
                val array = authorJson.asJsonArray
                if (array.size() > 0) {
                    if (array[0].isJsonPrimitive)
                        authors = array.map { getAuthorFromName(it.asString) }.toTypedArray()
                    else if (array[0].isJsonObject)
                        authors = array.map { getAuthorFromName(it.asJsonObject["name"].asString) }.toTypedArray()
                }
            }
        }

        if (authors != null && authors.isNotEmpty())
            return authors
        if (author != null)
            return arrayOf(getAuthorFromName(author))

        authors = getAuthorFromXML()

        if (authors != null && authors.isNotEmpty())
            return authors

        authors = doc.select("[data-authorname]")
            .map { authorMatcher.evaluateString(it.attr("data-authorname"))?.value ?: arrayOf(getAuthorFromName(it.text())) }
            .flatMap { it.asIterable() }
            .distinct()
            .toTypedArray()

        if (authors.isNotEmpty())
            return authors

        authors = doc.select("div[data-share-authors]")
            .filter { it.attr("data-share-authors").isNotBlank() }
            .map {
                val authorsJson = JsonParser().parse(it.attr("data-share-authors").replace("&quot;","\"")).asJsonArray
                authorsJson.map {
                    Author(it.asJsonObject.get("first_name").asString, it.asJsonObject.get("last_name").asString)
                }.toTypedArray()
            }
            .flatMap { it.asIterable() }
            .distinct()
            .toTypedArray()

        if (authors.isNotEmpty())
            return authors

        authors = doc.select("[itemProp='author creator'], .author, .ArticlePage-authorName, .story-meta__authors .vcard")
            .map { authorMatcher.evaluateString(it.text())?.value ?: arrayOf(getAuthorFromName(it.text())) }
            .flatMap { it.asIterable() }
            .distinct()
            .toTypedArray()

        if (authors.isNotEmpty())
            return authors

        authors = doc.select("p[itemprop='author'] [itemprop='name']")
            .map { authorMatcher.evaluateString(it.text())?.value ?: arrayOf(getAuthorFromName(it.text())) }
            .flatMap { it.asIterable() }
            .distinct()
            .toTypedArray()

        if (authors.isNotEmpty())
            return authors

        authors = doc.select("div[itemprop='author'] [itemprop='name']")
            .map { authorMatcher.evaluateString(it.attr("content"))?.value ?: arrayOf(getAuthorFromName(it.attr("content"))) }
            .flatMap { it.asIterable() }
            .distinct()
            .toTypedArray()

        if (authors.isNotEmpty())
            return authors

        authors = doc.select("header .meta-line")
            .map { authorMatcher.evaluateString(it.text())?.value ?: arrayOf()}
            .flatMap { it.asIterable() }
            .distinct()
            .toTypedArray()

        if (authors.isNotEmpty())
            return authors

        return authorMatcher.evaluateDoc(doc)?.value
    }

    private fun convertIsoToHumanReadable(date: String): Timestamp {
        var dateChanging = date.toLowerCase()
                .replace(Regex("(Mon|Tue|Wed|Thu|Fri|Sat|Sun)"), "")
                .replace(Regex("\\d\\d:\\d\\d:\\d\\d"), "")
                .replace(Regex("\\d\\d:\\d\\d"), "")
                .replace("gmt", "")
                .replace(",","")

        val ts = Timestamp()

        for (i in 2000..2019) {
            if (dateChanging.contains(i.toString())) {
                ts.year.set(i.toString())
                dateChanging = dateChanging.replace(i.toString(), "")
                break
            }
        }

        val months = arrayOf(
                arrayOf("january", "jan", 1),
                arrayOf("february", "feb", 2),
                arrayOf("march", "mar", 3),
                arrayOf("april", "apr", 4),
                arrayOf("may", "may", 5),
                arrayOf("june", "jun", 6),
                arrayOf("july", "jul", 7),
                arrayOf("august", "aug", 8),
                arrayOf("september", "sep", 9),
                arrayOf("october", "oct", 10),
                arrayOf("november", "nov", 11),
                arrayOf("december", "dec", 12)
        )

        for ((full, abbr, number) in months) {
            var found = false
            if (dateChanging.contains(full as String)) {
                found = true
                dateChanging = dateChanging.replace(full, "")
            } else if (dateChanging.contains(abbr as String)) {
                found = true
                dateChanging = dateChanging.replace(abbr, "")
            }

            if (found) {
                ts.month.set((number as Int).toString())
                dateChanging = dateChanging.replace(Regex("[^0-9]"), "")
                ts.day.set(dateChanging)
            }
        }

        return ts
    }

    fun getDate(): Timestamp {
        // Try to get the date in ISO format from the metadata information
        var dateISO = findMeta("analyticsAttributes.articleDate",
                "sailthru.date", "pubdate", "og:pubdate", "og:article:published_time", "og:article:modified_time",
                "datePublished", "dateModified", "article:published_time")

        if (dateISO == null) {
            if (metaJson.has("datePublished"))
                dateISO = metaJson["datePublished"].asString
            else if (metaJson.has("datedCreated"))
                dateISO = metaJson["dateCreated"].asString
            else if (metaJson.has("dateModified"))
                dateISO = metaJson["dateModified"].asString
        }

        if (getHostName(url) == "huffpost") {
            val info = findMeta("article:published_time")!!.split(" ")[0].split("-")
            val ts = Timestamp()
            ts.year.set(info[0].toInt().toString())
            ts.month.set(info[1].toInt().toString())
            ts.day.set(info[2].toInt().toString())
            return ts
        }

        // If it was found, parse it
        if (dateISO != null) {
            val endDate = Regex("[^0-9\\- ]").find(dateISO)?.range?.first ?: "0000-00-00".length
            val sections: List<String>

            // Format YYYYMMDD e.g. 20190301
            if (dateISO.none { !it.isDigit() } && dateISO.length == 8) {
                sections = listOf(
                    dateISO.substring(0, 4),
                    dateISO.substring(4, 6),
                    dateISO.substring(6, 8)
                )
            } else {
                sections = dateISO.substring(0, endDate).split("-")
            }

            var ts: Timestamp
            try {
                ts = Timestamp()
                ts.year.set(sections[0].toInt().toString())
                ts.month.set(sections[1].toInt().toString())
                ts.day.set(sections[2].toInt().toString())
            } catch (e: Exception) {
                ts = convertIsoToHumanReadable(dateISO)
            }
            return ts
        }

        // Some random weird news site does it this way
        val humanReadable = doc.getElementsByTag("span")?.select("[itemprop=datePublished]")?.html()
        if (humanReadable != null && humanReadable.isNotEmpty())
            return convertIsoToHumanReadable(humanReadable)

        /*
        Try to parse it from the body of the text :(
        Regex is pretty cool.
        Format key:
        - M(W) = Month in words
        - DD(nd) = Day + st/nd/rd/th
        - Y = year (any digits)
        - YY(YY) = year (2 or 4 digits)

        1. M(W) DD(nd),Y
        (jan(uary)?|feb(ruary)?|mar(ch)?|apr(il)?|may|jun(e)?|jul(y)?|aug(ust)?|sep(t(ember)?)?|oct(ober)?|nov(ember)?|dec(ember)?)[,.]?\s([0-9][0-9]?)(st|nd|rd|th)?[ ,.]+([0-9]+)?

        2. DD(nd) M(W),Y
        ([0-9][0-9]?)(st|nd|rd|th)?\s(jan(uary)?|feb(ruary)?|mar(ch)?|apr(il)?|may|jun(e)?|jul(y)?|aug(ust)?|sep(t(ember)?)?|oct(ober)?|nov(ember)?|dec(ember)?)[ ,.]+([0-9]+)?

        3. YYYY,M(W) DD(nd)
        ([0-9][0-9][0-9][0-9])[ ,.]+(jan(uary)?|feb(ruary)?|mar(ch)?|apr(il)?|may|jun(e)?|jul(y)?|aug(ust)?|sep(t(ember)?)?|oct(ober)?|nov(ember)?|dec(ember)?)[,.]?\s([0-9][0-9]?)(st|nd|rd|th)?

        4. M(M)-D(D)-YY(YY)
        ([0-9][0-9]?)[-./ ]([0-9][0-9]?)[-./ ]([0-9][0-9]([0-9][0-9])?)

         */

        return dateMatcher.matchRegexDates(doc) ?: Timestamp()
    }

    private fun getHostName(url: String): String {
        val uri = URI(url)
        var hostname = uri.host
        // to provide faultproof result, check if not null then return only hostname, without www.
        if (hostname != null) {
            if (hostname.startsWith("http://")) hostname = hostname.substring("http://".length + 1)
            if (hostname.startsWith("https://")) hostname = hostname.substring("https://".length + 1)

            if (hostname.startsWith("www")) hostname = hostname.substring(hostname.indexOf('.') + 1)

            // remove the .com / .org / .net
            hostname = hostname.substring(0, hostname.lastIndexOf('.'))
        } else hostname = "No Publication"
        return hostname.toLowerCase()
    }

    fun getPublication(): String {
        if (this.publisher == null) {
            val hostname = getHostName(url).toLowerCase()
            if (hostname == "nytimes")
                return "The New York Times"
            else if (hostname == "apnews")
                return "Associated Press"
            else if (hostname == "thediplomat")
                return "The Diplomat"
            else if (hostname == "worldpoliticsreview")
                return "World Politics Review"
            else if (hostname == "npr")
                return "National Public Radio"
            else if (hostname == "sciencedirect")
                return doc.select(".publication-title-link").text()
            else if (hostname == "prisonpolicy")
                return "Prison Policy Institute"

            if (metaJson.has("publisher")) {
                publisher =
                    if (metaJson["publisher"].isJsonArray)
                        metaJson["publisher"].asJsonArray[0].asJsonObject["name"]?.asString ?: "No Publication Found"
                    else if (metaJson["publisher"].isJsonPrimitive)
                        metaJson["publisher"].asString
                    else
                        metaJson["publisher"].asJsonObject["name"]?.asString ?: "No Publication Found"

                return publisher as String
            }

            publisher = findMeta("og:site_name")
            if (publisher != null)
                return publisher as String

            publisher = getHostName(url).capitalize()
        }
        return StringEscapeUtils.unescapeHtml4(publisher as String)
    }

    fun getTitle(): String? {
        if (titleString == null) {
            if (getPublication() == "Associated Press") {
                return StringEscapeUtils.unescapeHtml4(doc.select("h1").text())
            } else if (getPublication() == "SAGE Journals") {
                return StringEscapeUtils.unescapeHtml4(doc.select("h1").text())
            } else if (getPublication() == "World Politics Review") {
                return StringEscapeUtils.unescapeHtml4(doc.select("h1").text())
            } else if (getHostName(url) == "sciencedirect") {
                return StringEscapeUtils.unescapeHtml4(doc.select("h1").text())
            } else if (getHostName(url) == "thecrimereport") {
                return StringEscapeUtils.unescapeHtml4(doc.select("h1").text())
            } else if (getHostName(url) == "e-flux") {
                return StringEscapeUtils.unescapeHtml4(doc.select("h1").text())
            }

            if (metaJson.has("headline")) {
                titleString = metaJson["headline"].asString
                return StringEscapeUtils.unescapeHtml4(titleString)
            }

            val headline: String? = findMeta("parsely-title", "og:title", "sailthru.title", "analyticsAttributes.title")
            if (headline != null) {
                titleString = headline
                return StringEscapeUtils.unescapeHtml4(titleString)
            }

            titleString = doc.getElementsByTag("title").maxBy { it.text().length }?.text()
        }
        return StringEscapeUtils.unescapeHtml4(titleString)
    }
    fun getURL() = url

    fun getBodyParagraphsText(): MutableList<String> {
        if (cardrUI?.overrideBodyParagraphs != null)
            return cardrUI!!.overrideBodyParagraphs!!
        if (bodyParagraphElements == null) {
            val reader = CardBodyReader(getHostName(url).toLowerCase(), doc)
            bodyParagraphElements = reader.getBodyParagraphs(cardID != null)
        }
        return (bodyParagraphElements as Elements).map { it.text() }.toMutableList()
    }

    fun getBodyParagraphText(html: Boolean): String {
        if (html) {
            val sb = StringBuilder()
            getBodyParagraphsText().forEach {
                sb.append("<p>")

                sb.append(it)
                sb.append(' ')

                sb.append("</p>")
            }
            return sb.toString()
        } else {
            val sb = StringBuilder()
            getBodyParagraphsText().forEach {
                sb.append(it)
                sb.append(' ')
                if (!Prefs.get().condense)
                    sb.append('\n')
            }
            return sb.toString()
        }
    }
}