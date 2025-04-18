package org.sonso.bludceapi.service

import org.slf4j.LoggerFactory
import org.sonso.bludceapi.dto.ReceiptPosition
import org.sonso.bludceapi.dto.response.ReceiptPositionResponse
import org.sonso.bludceapi.dto.response.ReceiptPositionSaveResponse
import org.sonso.bludceapi.entity.ReceiptEntity
import org.sonso.bludceapi.entity.UserEntity
import org.sonso.bludceapi.repository.ReceiptPositionRepository
import org.sonso.bludceapi.repository.ReceiptRepository
import org.sonso.bludceapi.util.toReceiptPositionEntity
import org.sonso.bludceapi.util.toReceiptPositionResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ReceiptPositionService(
    private val receiptPositionRepository: ReceiptPositionRepository,
    private val receiptRepository: ReceiptRepository
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun getById(id: UUID): ReceiptPositionResponse {
        log.debug("Getting ReceiptPosition by id: $id")
        return receiptPositionRepository.findById(id)
            .orElseThrow { NoSuchElementException("ReceiptPosition with ID $id not found") }
            .toReceiptPositionResponse()
    }

    fun getAll(): List<ReceiptPositionResponse> {
        log.info("Request to get all ReceiptPosition")
        return receiptPositionRepository.findAll()
            .map { it.toReceiptPositionResponse() }
    }

    @Transactional
    fun saveAll(request: List<ReceiptPosition>, currentUser: UserEntity): ReceiptPositionSaveResponse {
        log.info("Saving ReceiptPositions")

        val receipt = receiptRepository.save(ReceiptEntity().apply { initiator = currentUser })

        var receiptPositions = request
            .map { it.toReceiptPositionEntity(receipt) }

        val saveReceiptPosition = receiptPositionRepository.saveAll(receiptPositions)

        log.info("Saved ReceiptPositions successful")
        log.debug("Saved ReceiptPositions: $saveReceiptPosition")

        return ReceiptPositionSaveResponse(receipt.id)
    }

    @Transactional
    fun delete(id: UUID): ReceiptPositionResponse {
        log.info("Deleting ReceiptPosition")
        log.debug("Deleting ReceiptPosition with id: $id")

        val existingReceiptPosition = receiptPositionRepository.findById(id)
            .orElseThrow { NoSuchElementException("ReceiptPosition with id: $id not found") }

        receiptPositionRepository.delete(existingReceiptPosition)

        log.info("Deleting ReceiptPosition successful")
        return existingReceiptPosition.toReceiptPositionResponse()
    }
}
