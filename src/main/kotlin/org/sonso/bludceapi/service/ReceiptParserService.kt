package org.sonso.bludceapi.service

import org.slf4j.LoggerFactory
import org.sonso.bludceapi.client.OcrServiceClient
import org.sonso.bludceapi.config.properties.GigachatPrompts
import org.sonso.bludceapi.dto.OcrError
import org.sonso.bludceapi.dto.ReceiptPosition
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ReceiptParserService(
    private val orcServiceClient: OcrServiceClient,
    private val gigachatService: GigachatService,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun getImageFromText(image: MultipartFile): List<ReceiptPosition> {
        log.info("Sending image to OCR service: ${image.originalFilename}, size: ${image.size} bytes")

        val response = orcServiceClient.getTextFromImage(image)

        require(response.ocrExitCode != OcrError.SIZE_LIMIT) {
            "Ошибка. Размер файла превышает максимально допустимый 1024 КБ"
        }

        val ocrText = response.parsedResults?.firstOrNull()?.parsedText.orEmpty()

        log.info("Request GIGACHAT for receipt parsing ")
        val receiptPositions = gigachatService.correctReceiptPosition(GigachatPrompts.receiptOcrToJsonPosition(ocrText))

        log.info("Request GIGACHAT correct name of receipt positions")
        val result = gigachatService.correctReceiptPosition(GigachatPrompts.correctPositionName(receiptPositions))

        log.info("Extracted ${result.size} item(s) from parsed text")
        return result
    }
}
