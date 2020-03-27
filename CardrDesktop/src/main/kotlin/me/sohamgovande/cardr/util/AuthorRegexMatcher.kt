package me.sohamgovande.cardr.util

import org.apache.logging.log4j.LogManager
import org.jsoup.nodes.Document
import java.lang.Exception

class AuthorRegexMatcher {

    private val regexs: MutableList<Pair<String, (MatchGroupCollection) -> AuthorList>> = mutableListOf()

    fun register(string: String, processor: (MatchGroupCollection) -> AuthorList) =
        regexs.add(Pair(string, processor))

    fun evaluateString(str: String): AuthorList? {
        for (pair in regexs) {
            val matchResult = Regex(pair.first).find(str) ?: continue
            try {
                return pair.second(matchResult.groups)
            } catch (e: Exception) {
                logger.error("Error evaluating author name in String", e)
            }
        }
        return null
    }

    fun evaluateDoc(doc: Document): AuthorList? {
        for (pair in regexs) {
            val elements = doc.select("*:matchesOwn(${pair.first})")
            if (elements.size == 0) continue

            for (element in elements) {
                if (element.hasClass("HeroTextBelow-description"))
                    continue

                val matchResult = Regex(pair.first).find(element.text()) ?: continue

                try {
                    return pair.second(matchResult.groups)
                } catch (e: Exception) {
                    logger.error("Error processing author name in document", e)
                    continue
                }
            }
        }
        return null
    }

    companion object {
        val logger = LogManager.getLogger(AuthorRegexMatcher::class.java)
    }
}