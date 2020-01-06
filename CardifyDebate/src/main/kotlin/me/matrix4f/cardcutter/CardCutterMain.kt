package me.matrix4f.cardcutter

import javafx.application.Application
import me.matrix4f.cardcutter.ui.CardCuttingUI
import me.matrix4f.cardcutter.web.WebsiteCardCutter

lateinit var ui: CardCuttingUI

fun main(args: Array<String>) {
    if (args.size == 1) {
        Thread {
            val reader = WebsiteCardCutter(args[0])
            println(args[0])
            while (!ui.loaded) { }
            ui.loadFromReader(reader)
        }.start()
    }
    Application.launch(CardifyDebate::class.java)
}