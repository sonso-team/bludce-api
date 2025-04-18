package org.sonso.bludceapi.service

import org.slf4j.LoggerFactory
import org.sonso.bludceapi.dto.TipsType
import org.sonso.bludceapi.dto.request.ReceiptUpdateRequest
import org.sonso.bludceapi.dto.response.ReceiptResponse
import org.sonso.bludceapi.entity.UserEntity
import org.sonso.bludceapi.repository.ReceiptRepository
import org.sonso.bludceapi.util.toReceiptResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
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
        log.info("Request to get all Receipt by initiator id")
        log.debug("Request to get all Receipt by initiator id: $initiatorId")
        return receiptRepository.findByInitiatorId(initiatorId)
            ?.map { it.toReceiptResponse() }
    }

    fun getById(id: UUID): ReceiptResponse {
        log.info("Request to get Receipt by id")
        log.debug("Request to get Receipt by id: $id")
        return receiptRepository.findById(id)
            .orElseThrow { NoSuchElementException("Чек с id $id не найден") }
            .toReceiptResponse()
    }

    fun getAll(): List<ReceiptResponse> {
        log.info("Request to get all Receipt")
        return receiptRepository.findAll()
            .map { it.toReceiptResponse() }
    }

    @Transactional
    fun update(request: ReceiptUpdateRequest, currentUser: UserEntity) {
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
            personCount = request.personCount
            tipsPercent = request.tipsPercent.takeIf { isTipsPercentAllowed(tipsType) }
            tipsValue = request.tipsValue.takeIf { isTipsValueAllowed(tipsType) }
            updatedAt = LocalDateTime.now()

            tipsAmount = when (request.tipsType) {
                TipsType.NONE, TipsType.PROPORTIONALLY -> 0.0
                else -> tipsValue!!.toDouble() * personCount
            }.toBigDecimal()

            totalAmount = this.positions
                .map { it.price.multiply(it.quantity.toBigDecimal()) }
                .fold(BigDecimal.ZERO, BigDecimal::add)
        }
        receiptRepository.save(receipt)
    }

    @Transactional
    fun delete(id: UUID): ReceiptResponse {
        log.info("Deleting Receipt")
        log.debug("Deleting Receipt position with id: $id")

        val existingReceipt = receiptRepository.findById(id)
            .orElseThrow { NoSuchElementException("Чек с id: $id не найден") }

        receiptRepository.delete(existingReceipt)

        log.info("Deleting Receipt successful")
        return existingReceipt.toReceiptResponse()
    }

    fun isTipsPercentAllowed(tipsType: TipsType): Boolean =
        tipsType == TipsType.PROPORTIONALLY

    fun isTipsValueAllowed(tipsType: TipsType): Boolean =
        tipsType == TipsType.EVENLY

    private fun validateUpdate(updateRequest: ReceiptUpdateRequest) {
        val tipsType = updateRequest.tipsType
        val tipsPercent = updateRequest.tipsPercent
        val tipsValue = updateRequest.tipsValue

        val isPercentAllowed = isTipsPercentAllowed(tipsType)
        val isValueAllowed = isTipsValueAllowed(tipsType)

        val isTipsMismatch = when (tipsType) {
            TipsType.FOR_KICKS ->
                tipsPercent != null || tipsValue != null

            else -> {
                val isPercentValid = if (isPercentAllowed) tipsPercent?.let { it > 0 } == true else tipsPercent == null
                val isValueValid =
                    if (isValueAllowed) tipsValue?.let { it > BigDecimal.ZERO } == true else tipsValue == null
                !isPercentValid || !isValueValid
            }
        }

        require(!isTipsMismatch) {
            "Некорректные данные: несоответствие между типом чаевых, процентом и/или значением"
        }
    }
}
