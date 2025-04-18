package org.sonso.bludceapi.dto.response

import org.sonso.bludceapi.dto.ReceiptPosition
import org.sonso.bludceapi.dto.ReceiptType
import org.sonso.bludceapi.dto.TipsType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class ReceiptResponse(
    val id: UUID,
    val receiptType: ReceiptType,
    val tipsType: TipsType,
    val tipsValue: BigDecimal?,
    val tipsPercent: Int?,
    val personCount: Int?,
    val totalAmount: BigDecimal,
    val tipsAmount: BigDecimal,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val initiatorName: String,
    val positions: List<ReceiptPosition>
)
