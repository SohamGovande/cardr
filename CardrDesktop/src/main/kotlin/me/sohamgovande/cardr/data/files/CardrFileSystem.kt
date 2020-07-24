package me.sohamgovande.cardr.data.files

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

object CardrFileSystem {

    private val logger = LogManager.getLogger(CardrFileSystem::class.java)
    private val foldersJsonPath = Paths.get(System.getProperty("cardr.data.dir"), "cards", "Folders.json")

    val folders = mutableListOf<FSFolder>()
    val cards = mutableListOf<CardData>()

    private val gson = GsonBuilder().setPrettyPrinting().setLenient().create()

    fun read() {
        val folder = Paths.get(System.getProperty("cardr.data.dir"), "cards")
        try { Files.createDirectories(folder) } catch (e: FileAlreadyExistsException) { }

        // Load all folders
        if (Files.exists(foldersJsonPath)) {
            val foldersArray = JsonParser().parse(String(Files.readAllBytes(foldersJsonPath))).asJsonArray
            for (folderRawObj in foldersArray) {
                val parsedFolder: FSFolder? = gson.fromJson(folderRawObj, FSFolder::class.java)
                if (parsedFolder != null)
                    folders.add(parsedFolder)
                else
                    logger.error("Unable to parse folder from json data $folderRawObj")
            }
        }

        // Load all cards
        val cardFiles = folder.toFile().listFiles { _, name -> name.toLowerCase().endsWith(".card") }
        if (cardFiles != null) {
            for (cardFile in cardFiles) {
                val cardDataParsed = JsonParser().parse(String(Files.readAllBytes(Paths.get(cardFile.toURI())))).asJsonObject
                val cardData: CardData? = gson.fromJson(cardDataParsed, CardData::class.java)

                if (cardData != null) {
                    cardData.filename = cardFile.nameWithoutExtension
                    cardData.setProperties(cardDataParsed["properties"].asJsonObject)
                    cards.add(cardData)
                } else {
                    logger.error("Unable to parse card from json data $cardData")
                }
            }
        }
    }

    fun saveFolders() {
        val jsonArray = JsonArray()
        for (folder in  folders)
            jsonArray.add(gson.toJson(folder, FSFolder::class.java))
        Files.write(
            foldersJsonPath,
            gson.toJson(jsonArray).toByteArray()
        )
    }

    fun saveCard(card: CardData) {
        if (card.filename == null || card.filename == "")
            card.filename = card.uuid.toString()
        val jsonRaw = gson.toJson(card, CardData::class.java)
        val jsonParsed = JsonParser().parse(jsonRaw).asJsonObject
        jsonParsed.add("properties", card.getPropertiesJson())
        Files.write(
            Paths.get(System.getProperty("cardr.data.dir"), "cards", "${card.filename}.card"),
            gson.toJson(jsonParsed).toByteArray()
        )
    }

    fun findCard(uuid: UUID): CardData = cards.first { it.uuid == uuid }
}
