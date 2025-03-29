package org.sonso.bludceapi.service

import org.slf4j.LoggerFactory
import org.sonso.bludceapi.client.OcrServiceClient
import org.sonso.bludceapi.dto.ReceiptItemResponse
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ReceiptParserService(
    private val orcServiceClient: OcrServiceClient
) {

    private val log = LoggerFactory.getLogger(ReceiptParserService::class.java)

    fun getImageFromText(image: MultipartFile): List<ReceiptItemResponse> {
        log.info("Sending image to OCR service: ${image.originalFilename}, size: ${image.size} bytes")

        val response = orcServiceClient.getTextFromImage(image)
        val parsedText = response.parsedResults.firstOrNull()?.parsedText.orEmpty()

        val lines = parsedText
            .split("\n", "\r")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val items = mutableListOf<ReceiptItemResponse>()

        val linesColumns: MutableList<List<String>> = mutableListOf()
        for (line in lines) {
            val columns = line
                .split("\t")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            if (columns.size < 2) {
                continue
            }
            linesColumns.add(columns)
        }

        for (columns in linesColumns) {
            val priceStr = columns.last()
            val price = try {
                priceStr.replace(',', '.').toDouble()
            } catch (e: NumberFormatException) {
                continue
            }

            val quantityStr = if (columns.size > 2) columns[columns.size - 2] else null
            val quantity = quantityStr?.let {
                try {
                    it.replace(',', '.').toDouble()
                } catch (e: NumberFormatException) {
                    null
                }
            } ?: 1.0

            val name: String = if (quantityStr != null && quantityStr == columns[columns.size - 2]) {
                columns.subList(0, columns.size - 2).joinToString(" ")
            } else {
                columns.subList(0, columns.size - 1).joinToString(" ")
            }

            items.add(
                ReceiptItemResponse(
                    name = name,
                    quantity = quantity,
                    price = price
                )
            )
        }

        log.info("Extracted ${items.size} item(s) from parsed text")
        return items
    }
}
