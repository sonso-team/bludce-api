package org.sonso.bludceapi.repository

import org.sonso.bludceapi.entity.ReceiptPositionEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ReceiptPositionRepository : CrudRepository<ReceiptPositionEntity, UUID> {
    @Query(
        value = "SELECT * FROM receipt_position WHERE id IN (:ids)",
        nativeQuery = true
    )
    fun findAllById(@Param("ids") ids: List<UUID>): List<ReceiptPositionEntity>
}
