package org.sonso.bludceapi.service

import org.slf4j.LoggerFactory
import org.sonso.bludceapi.client.OcrServiceClient
import org.sonso.bludceapi.dto.ReceiptPosition
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ReceiptParserService(
    private val orcServiceClient: OcrServiceClient,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun getImageFromText(image: MultipartFile): List<ReceiptPosition> {
        log.info("Отправка изображения в OCR сервис: ${image.originalFilename}, размер: ${image.size} байт")

        val response = orcServiceClient.getTextFromImage(image)
        val parsedText = response.parsedResults.firstOrNull()?.parsedText.orEmpty()

        val lines = parsedText
            .split("\n", "\r")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val items = mutableListOf<ReceiptPosition>()

        val linesColumns: MutableList<List<String>> = mutableListOf()
        lines.forEach { line ->
            linesColumns.add(
                line
                    .split("\t")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .takeIf { it.size >= 2 }
                    ?: return@forEach
            )
        }

        linesColumns.forEach { columns ->
            val priceStr = columns.last()
            val price = try {
                priceStr.replace(',', '.').toBigDecimal()
            } catch (e: NumberFormatException) {
                log.debug("Некорректная цена $priceStr")
                return@forEach
            }

            val quantityStr = columns[columns.size - 2].takeIf { columns.size > 2 }
            val quantity = quantityStr?.let {
                try {
                    it.replace(',', '.').toInt()
                } catch (e: NumberFormatException) {
                    null
                }
            } ?: 1

            val name = if (quantityStr != null && quantityStr == columns[columns.size - 2]) {
                columns.subList(0, columns.size - 2).joinToString(" ")
            } else {
                columns.subList(0, columns.size - 1).joinToString(" ")
            }

            items.add(
                ReceiptPosition(
                    name = name,
                    quantity = quantity,
                    price = price
                )
            )
        }

        log.info("Извлечено ${items.size} элементов из проанализированного текста")
        return items
    }
}
