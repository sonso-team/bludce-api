package org.sonso.bludceapi.dto.request

import org.sonso.bludceapi.dto.ReceiptType
import org.sonso.bludceapi.dto.TipsType
import java.util.UUID

data class ReceiptRequest(
    val receiptType: ReceiptType,
    val tipsType: TipsType,
    val tipsPercent: Int,
    val personCount: Int,
    val receiptPositionIds: List<UUID>
)
