package org.sonso.bludceapi.dto.response

import org.sonso.bludceapi.config.properties.ReceiptType
import org.sonso.bludceapi.config.properties.TipsType
import org.sonso.bludceapi.dto.ReceiptPosition
import org.sonso.bludceapi.dto.User
import java.util.*

data class ReceiptResponse(
    val id: UUID,
    val receiptType: ReceiptType,
    val tipsType: TipsType?,
    val tipsPercent: Int?,
    val personCount: Int?,
    val initiator: User,
    val positions: List<ReceiptPosition>
)
