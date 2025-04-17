package org.sonso.bludceapi.service

import org.slf4j.LoggerFactory
import org.sonso.bludceapi.dto.ReceiptType
import org.sonso.bludceapi.dto.TipsType
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

    fun getById(id: UUID): ReceiptResponse {
        log.debug("Request to get Receipt by id: $id")
        return receiptRepository.findById(id)
            .orElseThrow { NoSuchElementException("Receipt with ID $id not found") }
            .toReceiptResponse()
    }

    fun getAll(): List<ReceiptResponse> {
        log.info("Request to get all Receipt")
        return receiptRepository.findAll()
            .map { it.toReceiptResponse() }
    }

    @Transactional
    fun update(request: ReceiptUpdateRequest, currentUser: UserEntity): Map<String, String> {
        validateUpdate(request)

        val receipt = receiptRepository.findById(request.receiptId)
            .orElseThrow { NoSuchElementException("Receipt with ID ${request.receiptId} not found") }

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
        log.info("Deleting Receipt")
        log.debug("Deleting Receipt position with id: $id")

        val existingReceipt = receiptRepository.findById(id)
            .orElseThrow { NoSuchElementException("Receipt with id: $id not found") }

        receiptRepository.delete(existingReceipt)

        log.info("Deleting Receipt successful")
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
