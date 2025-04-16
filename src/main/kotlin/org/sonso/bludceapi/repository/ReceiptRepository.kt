package org.sonso.bludceapi.repository

import org.sonso.bludceapi.entity.ReceiptEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ReceiptRepository : CrudRepository<ReceiptEntity, UUID> {
    fun findByInitiatorId(initiatorId: UUID): List<ReceiptEntity>?
}
