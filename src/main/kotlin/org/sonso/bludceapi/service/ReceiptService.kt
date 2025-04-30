package org.sonso.bludceapi.service

import org.slf4j.LoggerFactory
import org.sonso.bludceapi.dto.ReceiptType
import org.sonso.bludceapi.dto.TipsType
import org.sonso.bludceapi.dto.request.FinishRequest
import org.sonso.bludceapi.dto.request.ReceiptUpdateRequest
import org.sonso.bludceapi.dto.response.FinishResponse
import org.sonso.bludceapi.dto.response.ReceiptResponse
import org.sonso.bludceapi.dto.ws.Payload
import org.sonso.bludceapi.entity.UserEntity
import org.sonso.bludceapi.repository.jpa.ReceiptRepository
import org.sonso.bludceapi.repository.redis.PayedUserRedisRepository
import org.sonso.bludceapi.repository.redis.ReceiptRedisRepository
import org.sonso.bludceapi.util.toReceiptResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class ReceiptService(
    private val receiptRepository: ReceiptRepository,
    private val receiptRedisRepository: ReceiptRedisRepository,
    private val payedUserRedisRepository: PayedUserRedisRepository,
    private val lobbySocketHandler: LobbySocketHandler,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun getAllByInitiatorId(initiatorId: UUID): List<ReceiptResponse>? {
        log.info("Request to get all Receipt by initiator id")
        log.debug("Request to get all Receipt by initiator id: {}", initiatorId)
        return receiptRepository.findByInitiatorId(initiatorId)
            ?.map { it.toReceiptResponse() }
    }

    fun getById(id: UUID): ReceiptResponse {
        log.info("Request to get Receipt by id")
        log.debug("Request to get Receipt by id: {}", id)
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

            tipsAmount = if (tipsType == TipsType.EVENLY) {
                tipsValue!!.toDouble() * personCount
            } else {
                0.0
            }.toBigDecimal()

            totalAmount = this.positions
                .map { it.price.multiply(it.quantity.toBigDecimal()) }
                .fold(BigDecimal.ZERO, BigDecimal::add)
        }
        receiptRepository.save(receipt)
    }

    @Transactional
    fun delete(id: UUID): ReceiptResponse {
        val existingReceipt = receiptRepository.findById(id)
            .orElseThrow { NoSuchElementException("Чек с id: $id не найден") }

        receiptRepository.delete(existingReceipt)

        log.info("Deleting Receipt $id successful")
        return existingReceipt.toReceiptResponse()
    }

    fun finish(
        lobbyId: UUID,
        userId: UUID,
        body: FinishRequest
    ): FinishResponse {
        val state = receiptRedisRepository.getState(lobbyId.toString())
        val receipt = receiptRepository.findById(lobbyId).orElseThrow()

        val payedUserState = payedUserRedisRepository.getState(lobbyId.toString()).toMutableList()
        payedUserState.add(userId.toString())
        payedUserRedisRepository.replaceState(lobbyId.toString(), payedUserState)

        val myPositions = state.filter { it.userId == userId }
        val amount = if (receipt.receiptType == ReceiptType.PROPORTIONALLY) {
            myPositions.fold(BigDecimal.ZERO) { acc, p ->
                acc + p.price.multiply(p.quantity.toBigDecimal())
            }
        } else {
            BigDecimal(receipt.totalAmount.toDouble() / receipt.personCount)
        }
        val tips = body.tips
        val total = amount + tips

        payedUserRedisRepository.removeUser(lobbyId.toString(), userId)

        // формируем новое состояние: отмечаем paidBy и освобождаем userId
        val newState = state.map {
            if (it.userId == userId) {
                Payload(
                    id = it.id,
                    name = it.name,
                    quantity = it.quantity,
                    price = it.price,
                    userId = userId,
                    paidBy = userId,
                )
            } else it
        }

        lobbySocketHandler.broadcastState(lobbyId.toString(), newState)
        return FinishResponse(amount, tips, total)
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
