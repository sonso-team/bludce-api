package org.sonso.bludceapi.dto.ws

import org.sonso.bludceapi.dto.ReceiptType
import org.sonso.bludceapi.dto.TipsType
import java.math.BigDecimal
import java.util.*

data class InitResponse(
    val type: String = "INIT",
    val userId: UUID,
    val initiatorId: UUID,
    val receiptType: ReceiptType,
    val tipsType: TipsType,
    val tipsAmount: BigDecimal?,
    val tipsPercent: Int?,
    val userAmount: BigDecimal?,
    val amount: BigDecimal,
    val fullAmount: BigDecimal,
    val state: List<Payload>
)
