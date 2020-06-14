package me.sohamgovande.cardr.core.ui.windows.markup

import org.apache.logging.log4j.LogManager
import java.lang.StringBuilder

data class ModifiableIntRange(var begin: Int, var end: Int)

class MarkupSection(var begin: Int, var end: Int) {

    var isBold: Boolean? = null
    var isUnderlined: Boolean? = null

    fun addTags(html: String, opening: String, closing: String, affectedSections: List<MarkupSection>): String {
        val builder = StringBuilder(html)
        builder.insert(begin, opening)
        builder.insert(end + opening.length, closing)

        val offset = opening.length + closing.length

        for (section in affectedSections) {
            if (section.begin > end) {
                section.begin += offset
                section.end += offset
            }
        }
        return builder.toString()
    }

    fun resolve(html: String): String {
        return html.substring(begin, end)
    }

    fun hasHTMLAttribute(html: String, tag: String, style: Regex?): Boolean {
        val tagsMatches = Regex("<\\s*[a-zA-Z-]+\\s*([a-zA-Z-]+\\s*=\\s*[\"'].+[\"']\\s*)+\\s*/?\\s*>").findAll(html)
        val tags = mutableListOf<String>()
        for (match in tagsMatches) {
            if (match.range.first > end)
                break

            if (match.value.startsWith("</")) {
                tags.removeAt(tags.size-1)
            } else {
                tags.add(match.value)
            }
        }
        val tagRegex = Regex("^<\\s*$tag\\s*([a-zA-Z-]+\\s*=\\s*[\"'].+[\"']\\s*)+\\s*/?\\s*>")
        if  (style == null) {
            return tags.any { it.matches(tagRegex) }
        } else {
            return tags.any {
                if (!it.matches(tagRegex))
                    return false
                val matchResult = Regex("(?<=style=\")([a-zA-Z-]+\\s*:\\s*[a-zA-Z0-9#' ]+;?\\s*)+(?=\")").find(it) ?: return false
                return matchResult.value.replace(Regex("\\s"),"").contains(style)
            }
        }
    }

    companion object {
        val logger = LogManager.getLogger(MarkupSection::class.java)

        fun findSectionsInSelection(html: String): List<MarkupSection> {
            val beginSelection = html.indexOf("<i>") + "<i>".length
            val endSelection = html.lastIndexOf("</i>")
            val sub = html.substring(beginSelection, endSelection)

            val matches = Regex("<[/]?\\w+[ a-zA-Z=\"]*>").findAll(sub)

            val tagRanges = matches.map { ModifiableIntRange(it.range.first, it.range.last) }.toMutableList()

            // Remove ranges that are right next to each other
            var i = 0
            while (i < tagRanges.size) {
                val range = tagRanges[i]
                if (i != tagRanges.size-1) {
                    val next = tagRanges[i+1]
                    if (next.begin == range.end+1) {
                        tagRanges.removeAt(i+1)
                        range.end = next.end
                        i--
                    }
                }
                i++
            }

            // Surround the ranges with the beginning and end of the selection
            val pairList = mutableListOf<Int>()
            for (range in tagRanges) {
                if (range.begin == range.end)
                    continue
                pairList.add(range.begin+beginSelection)
                pairList.add(range.end+beginSelection+1)
            }
            pairList.add(0, beginSelection)
            pairList.add(endSelection)

            val ret = mutableListOf<MarkupSection>()
            i = 0
            while (i < pairList.size) {
                val next = MarkupSection(pairList[i], pairList[i+1])
                if (next.resolve(html).isNotBlank())
                    ret.add(next)
                i += 2
            }

            val retResolved = ret.map {it.resolve(html)}.toList()

            return ret
        }
    }
}