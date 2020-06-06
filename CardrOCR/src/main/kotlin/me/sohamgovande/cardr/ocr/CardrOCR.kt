package me.sohamgovande.cardr.ocr

import net.sourceforge.tess4j.Tesseract
import java.nio.file.Files
import java.nio.file.Paths

class CardrOCR(val args: Array<String>) {

    val instance = Tesseract()

    init {
        instance.setDatapath(Paths.get(args[0], "ocr", "tessdata").toFile().absolutePath)
    }

    fun doOCR() {
        val result = instance.doOCR(Paths.get(args[0], "ocr", "ocr-region.png").toFile())
        Files.write(Paths.get(args[0], "ocr", "ocr-result.txt"), result.toByteArray())
    }
}