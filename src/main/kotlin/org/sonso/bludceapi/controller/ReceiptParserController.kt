package org.sonso.bludceapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.dto.response.ReceiptItemResponse
import org.sonso.bludceapi.service.ReceiptParserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/receipts")
@Tag(
    name = "Чеки",
    description = "Контроллер для работы с чеками"
)
class ReceiptParserController(
    private val receiptParserService: ReceiptParserService,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    @Operation(summary = "Загрузка чека и получение предварительных данных по нему")
    fun calculate(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<List<ReceiptItemResponse>> {
        log.info("File received: ${file.originalFilename}, size: ${file.size} bytes")
        return ResponseEntity(receiptParserService.getImageFromText(file), HttpStatus.OK)
    }
}
