package org.sonso.bludceapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.dto.request.ReceiptUpdateRequest
import org.sonso.bludceapi.dto.response.ReceiptResponse
import org.sonso.bludceapi.entity.UserEntity
import org.sonso.bludceapi.service.ReceiptService
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
@RequestMapping("/api/receipt")
@Tag(
    name = "Receipt API",
    description = "Основной контроллер по работе с чеком"
)
class ReceiptController(
    private val receiptService: ReceiptService
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{id}")
    @Operation(summary = "Получение чека по id")
    fun getReceipt(@PathVariable id: UUID): ResponseEntity<ReceiptResponse> {
        log.info("Request Receipt by id: $id")
        return ResponseEntity.ok(receiptService.getById(id))
    }

    @GetMapping
    @Operation(summary = "Получение всех чеков")
    fun getAll(): ResponseEntity<List<ReceiptResponse>> {
        log.info("Request get all Receipts")
        return ResponseEntity.ok(receiptService.getAll())
    }

    @PostMapping
    @Operation(summary = "Обновление конфигурации чека")
    fun update(
        @RequestBody request: ReceiptUpdateRequest,
        @AuthenticationPrincipal currentUser: UserEntity
    ): ResponseEntity<Map<String, String>> {
        log.info("Request save Receipts")
        return ResponseEntity.ok(receiptService.update(request, currentUser))
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Удаление чека по id")
    fun delete(@RequestParam id: UUID): ResponseEntity<ReceiptResponse> {
        log.info("Request delete Receipt")
        return ResponseEntity.ok(receiptService.delete(id))
    }
}
