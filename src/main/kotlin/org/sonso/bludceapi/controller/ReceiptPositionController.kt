package org.sonso.bludceapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.dto.ReceiptPosition
import org.sonso.bludceapi.dto.response.ReceiptPositionResponse
import org.sonso.bludceapi.dto.response.ReceiptPositionSaveResponse
import org.sonso.bludceapi.entity.UserEntity
import org.sonso.bludceapi.service.ReceiptPositionService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/receipt-position")
@Tag(
    name = "Receipt Position API",
    description = "Основной контроллер по работе с позициями в чеке"
)
class ReceiptPositionController(
    private val receiptPositionService: ReceiptPositionService
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{id}")
    @Operation(summary = "Получение позиции в чеке по id")
    fun getReceiptPosition(@PathVariable id: UUID): ResponseEntity<ReceiptPositionResponse> {
        log.info("Request ReceiptsPosition by id: $id")
        return ResponseEntity.ok(receiptPositionService.getById(id))
    }

    @GetMapping
    @Operation(summary = "Получение всех позиций в чеке")
    fun getAll(): ResponseEntity<List<ReceiptPositionResponse>> {
        log.info("Request get all ReceiptPosition")
        return ResponseEntity.ok(receiptPositionService.getAll())
    }

    @PostMapping
    @Operation(summary = "Добавление всех позиций в чеке")
    fun saveAll(
        @RequestBody request: List<ReceiptPosition>,
        @AuthenticationPrincipal currentUser: UserEntity
    ): ResponseEntity<ReceiptPositionSaveResponse> {
        log.info("Request save ReceiptPosition")
        return ResponseEntity.ok(receiptPositionService.saveAll(request, currentUser))
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Удаление позиции в чеке по id")
    fun delete(@RequestParam id: UUID): ResponseEntity<ReceiptPositionResponse> {
        log.info("Request delete ReceiptsPosition")
        return ResponseEntity.ok(receiptPositionService.delete(id))
    }
}
