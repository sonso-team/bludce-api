package org.sonso.bludceapi.repository

import org.sonso.bludceapi.entity.ReceiptPositionEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ReceiptPositionRepository : CrudRepository<ReceiptPositionEntity, UUID> {
    fun findAllByReceiptId(receiptId: UUID): List<ReceiptPositionEntity>
}
