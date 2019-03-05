package me.matrix4f.cardcutter.web

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import javafx.beans.property.SimpleStringProperty
import me.matrix4f.cardcutter.card.Author
import me.matrix4f.cardcutter.card.Timestamp
import me.matrix4f.cardcutter.util.*
import me.matrix4f.cardcutter.web.body.CardBodyReader
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.lang.Exception
import java.net.URI

class UrlDocReader(private val url: String) {

    private val doc: Document
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
        doc = Jsoup.connect(url).get()
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

    protected fun getAuthorFromName(name: String): Author {
        if (name.equals("BBC News"))
            return Author("", "BBC")

        val lastSpace = name.trim().lastIndexOf(' ')
        if (lastSpace == -1)
            return Author(SimpleStringProperty(""), SimpleStringProperty(name))
        return Author(name.substring(0, lastSpace).trim(),
                name.substring(lastSpace+1).trim())
    }

    protected fun getAuthorFromXML(): Array<Author>? {
        var authorStr: String = findMeta("author", "og:article:author") ?: return null
        if (authorStr.contains("www"))
            return null
        val parsed = authorMatcher.evaluateString(authorStr)
        if (parsed != null) return parsed.value

        val firstPunctuation = Regex("[^a-zA-Z &.()*]").find(authorStr)?.range?.first ?: authorStr.length
        authorStr = authorStr.substring(0, firstPunctuation)

        return arrayOf(getAuthorFromName(authorStr))
    }


    fun getAuthors(): Array<Author>? {
        var author: String? = null
        var authors: Array<Author>? = arrayOf()

        if (metaJson.has("author")) {
            val authorJson = metaJson["author"]
            if (authorJson.isJsonObject)
                author = authorJson.asJsonObject["name"].asString
            else if (authorJson.isJsonPrimitive)
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

        authors = doc.select("[itemProp='author creator'], .author, .ArticlePage-authorName")
            .map { authorMatcher.evaluateString(it.text())?.value ?: arrayOf(getAuthorFromName(it.text())) }
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

        // If it was found, parse it
        if (dateISO != null) {
            val endDate = Regex("[^0-9\\- ]").find(dateISO)?.range?.first ?: "0000-00-00".length
            val sections : List<String>

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

            var ts : Timestamp
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
        return hostname
    }

    fun getPublication(): String {
        if (this.publisher == null) {
            if (metaJson.has("publisher")) {

                val jsonPublisher =
                    if (metaJson["publisher"].isJsonArray)
                        metaJson["publisher"].asJsonArray[0].asJsonObject
                    else
                        metaJson["publisher"].asJsonObject

                publisher = jsonPublisher["name"].asString
                return publisher as String
            }

            publisher = findMeta("og:site_name")
            if (publisher != null)
                return publisher as String

            publisher = getHostName(url).capitalize()
        }
        return publisher as String

    }

    fun getTitle(): String? {
        if (titleString == null) {
            if (metaJson.has("headline")) {
                titleString = metaJson["headline"].asString
                return titleString
            }

            val headline: String? = findMeta("parsely-title", "og:title", "sailthru.title", "analyticsAttributes.title")
            if (headline != null) {
                titleString = headline
                return titleString
            }

            titleString = doc.getElementsByTag("title").maxBy { it.text().length }?.text()
        }
        return titleString
    }
    fun getURL() = url

    private fun getBodyParagraphs(): Elements {
        if (bodyParagraphElements == null) {
            val reader = CardBodyReader(getPublication().toLowerCase(), doc)
            bodyParagraphElements = reader.getBodyParagraphs()
        }
        return bodyParagraphElements as Elements
    }

    fun getBodyParagraphText(condensed: Boolean) : String {
        val sb = StringBuilder()
        getBodyParagraphs().forEach {
            if (!condensed)
                sb.append("")

            sb.append(it.text())
            sb.append(' ')

            if (!condensed)
                sb.append("")
        }
        sb.append("")
        return sb.toString()
    }
}