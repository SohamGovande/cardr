package me.matrix4f.cardcutter

import javafx.application.Application
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Scene
import javafx.stage.Stage
import me.matrix4f.cardcutter.card.Author
import me.matrix4f.cardcutter.card.Cite
import me.matrix4f.cardcutter.card.Timestamp
import me.matrix4f.cardcutter.platformspecific.MSWordInteractor
import me.matrix4f.cardcutter.ui.MainUI
import me.matrix4f.cardcutter.web.UrlDocReader
import java.util.*


fun testURLs() {
    val docs = arrayOf(
            "https://www.nytimes.com/2019/02/16/us/politics/trump-republican-party.html",
            "https://www.foxnews.com/opinion/trump-wins-on-border-security-with-emergency-declaration-and-funding-by-congress",
            "https://www.latimes.com/opinion/readersreact/la-ol-opinion-newsletter-trump-emergency-20190216-htmlstory.html",
            "https://www.nbcnews.com/news/us-news/intern-first-day-grandfather-eight-among-victims-shooting-aurora-illinois-n972521",
            "https://bangordailynews.com/2019/02/16/news/lewiston-auburn/alaska-state-troopers-arrest-auburn-man-in-connection-with-1993-murder/",
            "https://www.adn.com/alaska-news/crime-courts/2019/02/17/how-genealogists-helped-track-down-the-maine-man-accused-of-killing-sophie-sergie-nearly-26-years-ago/",
            "https://www.nydailynews.com/news/ny-metro-aoc-inauguration-amazon-20190216-story.html",
            "https://www.dailymail.co.uk/sport/football/article-6713365/Manchester-United-receive-whopping-3-8BN-takeover-bid-Saudi-Crown-Prince-Mohammad-bin-Salman.html",
            "https://www.thesun.co.uk/sport/football/8441062/saudi-prince-mohammad-bin-salman-manchester-united-takeover/",
            "https://www.reuters.com/article/us-asia-saudi-pakistan/saudi-crown-prince-heads-for-pakistan-amid-india-tensions-idUSKCN1Q604K",
            "https://www.aljazeera.com/indepth/opinion/pakistan-saudi-money-190215091327661.html"
    )


    var idx = 1
    for (url in docs) {
        val doc = UrlDocReader(url)
        println("- - - - - ${idx++}")
        println(Arrays.toString(doc.getAuthors()))
        println(doc.getDate())
        println(doc.getTitle())
        println(doc.getPublication())
        println(doc.getURL())
    }
}

var ui : MainUI? = null

class CardCutterApplication : Application() {
    override fun start(stage: Stage) {
        stage.title = "CardCutter for Debate"
        stage.resizableProperty().set(false)

        ui = MainUI()
        val panel = ui!!.initialize()

        stage.scene = Scene(panel, WIDTH, HEIGHT)
        stage.sizeToScene()
        stage.show()
    }

    companion object {
        const val WIDTH = 800.0
        const val HEIGHT = 600.0

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size == 1) {
                Thread {
                    val reader = UrlDocReader(args[0])
                    println(args[0])
                    while (ui?.loaded != true) {
                        // Wait
                    }
                    ui?.loadFromReader(reader)
                }.start()
            }
            launch(CardCutterApplication::class.java)
        }
    }
}