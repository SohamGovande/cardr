package me.sohamgovande.cardr.core.ui.windows.markup

import org.apache.logging.log4j.LogManager

class MarkupJavaConnector(private val window: MarkupCardWindow) {
    /**
     * Note: Do NOT rename this method without also renaming the JavaScript invoker
     */
    @Suppress("unused")
    fun emphasize() {
        window.emphasize()
    }

    @Suppress("unused")
    fun logError(error: String) {
        logger.error("Javascript Error: $error")
    }

    companion object {
        val logger = LogManager.getLogger(MarkupJavaConnector::class.java)
    }
}