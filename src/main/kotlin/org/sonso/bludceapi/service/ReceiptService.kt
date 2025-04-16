package org.sonso.bludceapi.service

import org.slf4j.LoggerFactory
import org.sonso.bludceapi.config.properties.ReceiptType
import org.sonso.bludceapi.config.properties.TipsType
import org.sonso.bludceapi.dto.request.ReceiptUpdateRequest
import org.sonso.bludceapi.dto.response.ReceiptResponse
import org.sonso.bludceapi.entity.UserEntity
import org.sonso.bludceapi.repository.ReceiptRepository
import org.sonso.bludceapi.util.toReceiptResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class ReceiptService(
    private val receiptRepository: ReceiptRepository
) {
    @Value("\${app.host}")
    lateinit var baseUrl: String

    private val log = LoggerFactory.getLogger(this::class.java)

    fun getAllByInitiatorId(initiatorId: UUID): List<ReceiptResponse>? {
        log.debug("Запрос на получение чека по id инициатора: $initiatorId")
        return receiptRepository.findByInitiatorId(initiatorId)
            ?.map { it.toReceiptResponse() }
    }

    fun getById(id: UUID): ReceiptResponse {
        log.debug("Запрос на получение чека по id: $id")
        return receiptRepository.findById(id)
            .orElseThrow { NoSuchElementException("Чек с id $id не найден") }
            .toReceiptResponse()
    }

    fun getAll(): List<ReceiptResponse> {
        log.info("Запрос на получение всех чеков")
        return receiptRepository.findAll()
            .map { it.toReceiptResponse() }
    }

    @Transactional
    fun update(request: ReceiptUpdateRequest, currentUser: UserEntity): Map<String, String> {
        validateUpdate(request)

        val receipt = receiptRepository.findById(request.receiptId)
            .orElseThrow { NoSuchElementException("Чек с ID ${request.receiptId} не найден") }

        require(currentUser.id == receipt.initiator.id) {
            "Ошибка, нельзя изменять не свой чек"
        }

        receipt.apply {
            initiator = currentUser
            receiptType = request.receiptType
            tipsType = request.tipsType
            personCount = request.personCount.takeIf { receiptType == ReceiptType.EVENLY }
            tipsPercent = request.tipsPercent.takeIf { isTipsPercentAllowed(tipsType) }
            updatedAt = LocalDateTime.now()
        }

        receiptRepository.save(receipt)

        return mapOf("urlConnection" to "$baseUrl/lobby/${receipt.id}")
    }

    @Transactional
    fun delete(id: UUID): ReceiptResponse {
        log.info("Удаление чека начато")
        log.debug("Удаление чека с id: $id начато")

        val existingReceipt = receiptRepository.findById(id)
            .orElseThrow { NoSuchElementException("Чек с id: $id не найден") }

        receiptRepository.delete(existingReceipt)

        log.info("Удаление чека прошло успешно")
        return existingReceipt.toReceiptResponse()
    }

    private fun validateUpdate(updateRequest: ReceiptUpdateRequest) {
        val tipsType = updateRequest.tipsType
        val tipsPercent = updateRequest.tipsPercent

        val isTipsMismatch =
            (tipsType in listOf(TipsType.EVENLY, TipsType.PROPORTIONALLY) && (tipsPercent?.let { it > 0 } != true)) ||
                (tipsType in listOf(TipsType.NONE, TipsType.FOR_KICKS) && (tipsPercent?.let { it > 0 } == true))

        require(!isTipsMismatch) {
            "Некорректные данные: тип чаевых не соответствует заданному проценту"
        }

        val receiptType = updateRequest.receiptType
        val personCount = updateRequest.personCount

        val isPersonMismatch =
            (receiptType == ReceiptType.EVENLY && (personCount?.let { it > 0 } != true)) ||
                (receiptType == ReceiptType.PROPORTIONALLY && (personCount?.let { it > 0 } == true))

        require(!isPersonMismatch) {
            "Некорректные данные: тип чека не соответствует заданному количеству персон"
        }
    }

    fun isTipsPercentAllowed(tipsType: TipsType): Boolean =
        tipsType == TipsType.EVENLY || tipsType == TipsType.PROPORTIONALLY
}
