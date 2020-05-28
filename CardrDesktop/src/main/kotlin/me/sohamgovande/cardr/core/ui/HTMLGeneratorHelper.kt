package me.sohamgovande.cardr.core.ui

import org.jsoup.nodes.Element

class HTMLGeneratorHelper {
    private val fontMapPreferred = mapOf(
        Pair("1","8"),
        Pair("2","10"),
        Pair("3","11"),
        Pair("4","13"),
        Pair("5","18"),
        Pair("6","24"),
        Pair("7","36"),
        Pair("large", "13")
    )
    private val fontMapNormal = mapOf(
        Pair("1","8"),
        Pair("2","10"),
        Pair("3","12"),
        Pair("4","14"),
        Pair("5","18"),
        Pair("6","24"),
        Pair("7","36"),
        Pair("large", "14")
    )

    fun applyFontSizeToSpanElem(span: Element) {
        if (span.hasAttr("style") && !span.attr("style").contains("font-size")) {
            val fontData = getCurrentFontMap(span)
            val fontSize = fontData.first

            span.attr("style","${span.attr("style")};font-size:$fontSize;")
        }
    }

    fun applyFontSizeToFontElem(font: Element) {
        val fontSize = getCurrentFontMap(font).first

        var style = ""
        if (font.hasAttr("face"))
            style += "font-family:'${font.attr("face")}';"

        style += "font-size:$fontSize;"

        font.tagName("span")
        font.attr("style", style + font.attr("style"))

        font.removeAttr("face")
        font.removeAttr("size")
    }

    private fun getCurrentFontMap(baseElem: Element): Pair<String, Map<String, String>> {
        val elemTree = arrayListOf<Element>()
        var parent = baseElem
        while (parent.tagName() != "body") {
            elemTree.add(parent)
            parent = parent.parent()
        }

        var useNormalMap = false

        var fontSize = "3"
        var fontSizeIsInt = true

        for (elem in elemTree) {
            println(elem.tagName())
            val style = elem.attr("style")
            if (style.contains("text-decoration: line-through")) {
                elem.attr("style", style.replace("text-decoration: line-through", ""))
                useNormalMap = true
            }
            if (elem.hasAttr("style") && elem.attr("style").contains("font-size")) {
                val matchResult = Regex("font-size: ([a-zA-Z0-9]+);").find(elem.attr("style"))
                if (matchResult != null) {
                    fontSize = matchResult.groups[1]!!.value
                    fontSizeIsInt = false
                }
            } else if (elem.hasAttr("size")) {
                fontSize = elem.attr("size")
            }
        }
        println("---")
        val fontMap = if (useNormalMap) fontMapNormal else fontMapPreferred
        if (fontSizeIsInt || fontSize == "large")
            fontSize = fontMap[fontSize] + "pt"
        return Pair(fontSize, fontMap)
    }
}