package org.sonso.bludceapi.dto.response

import org.sonso.bludceapi.dto.ReceiptType
import org.sonso.bludceapi.dto.TipsType
import org.sonso.bludceapi.dto.ReceiptPosition
import org.sonso.bludceapi.dto.User
import java.time.LocalDateTime
import java.util.*

data class ReceiptResponse(
    val id: UUID,
    val receiptType: ReceiptType,
    val tipsType: TipsType,
    val tipsPercent: Int?,
    val personCount: Int?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val initiator: User,
    val positions: List<ReceiptPosition>
)
