package org.sonso.bludceapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.dto.ReceiptPosition
import org.sonso.bludceapi.dto.request.FinishRequest
import org.sonso.bludceapi.dto.request.ReceiptUpdateRequest
import org.sonso.bludceapi.dto.response.FinishResponse
import org.sonso.bludceapi.dto.response.ReceiptResponse
import org.sonso.bludceapi.entity.UserEntity
import org.sonso.bludceapi.repository.RedisRepository
import org.sonso.bludceapi.service.LobbySocketHandler
import org.sonso.bludceapi.service.ReceiptParserService
import org.sonso.bludceapi.service.ReceiptService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal
import java.util.*

@RestController
@RequestMapping("/api/receipt")
@Tag(
    name = "Receipt API",
    description = "Основной контроллер по работе с чеком"
)
class ReceiptController(
    private val receiptService: ReceiptService,
    private val receiptParserService: ReceiptParserService,
    private val redisRepository: RedisRepository,
    private val lobbySocketHandler: LobbySocketHandler
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/history")
    @Operation(summary = "Получение всех чеков пользователя")
    fun getAllByInitiator(@AuthenticationPrincipal user: UserEntity): ResponseEntity<List<ReceiptResponse>?> {
        log.info("Request to receive all user receipts by id")
        return ResponseEntity.ok(receiptService.getAllByInitiatorId(user.id))
    }

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
    @Operation(summary = "Загрузка чека и получение предварительных данных по нему")
    fun calculate(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<List<ReceiptPosition>> {
        log.info("File received: ${file.originalFilename}, size: ${file.size} bytes")
        return ResponseEntity(receiptParserService.getImageFromText(file), HttpStatus.OK)
    }

    @PutMapping
    @Operation(summary = "Обновление конфигурации чека")
    fun update(
        @RequestBody request: ReceiptUpdateRequest,
        @AuthenticationPrincipal currentUser: UserEntity
    ): ResponseEntity<Map<String, String>> {
        log.info("Request save Receipts")
        receiptService.update(request, currentUser)
        return ResponseEntity(HttpStatus.OK)
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Удаление чека по id")
    fun delete(@RequestParam id: UUID): ResponseEntity<ReceiptResponse> {
        log.info("Request delete Receipt")
        return ResponseEntity.ok(receiptService.delete(id))
    }

    @PostMapping("/{id}/finish/{userId}")
    fun finish(
        @PathVariable id: UUID,
        @PathVariable userId: UUID,
        @RequestBody body: FinishRequest
    ): FinishResponse {
        val state = redisRepository.getState(id.toString())

        // позиции, выбранные текущим юзером
        val myPositions = state.filter { it.userId == userId }

        val amount = myPositions.fold(BigDecimal.ZERO) { acc, p ->
            acc + p.price.multiply(p.quantity.toBigDecimal())
        }
        val tips = body.tips
        val total = amount + tips

        // формируем новое состояние: отмечаем paidBy и освобождаем userId
        val newState = state.map {
            if (it.userId == userId)
                it.copy(userId = null, paidBy = userId)
            else it
        }

        lobbySocketHandler.broadcastState(id.toString(), newState)

        return FinishResponse(amount, tips, total)
    }
}
