package org.sonso.bludceapi.dto.request

import org.sonso.bludceapi.config.properties.ReceiptType
import org.sonso.bludceapi.config.properties.TipsType
import java.util.*

data class ReceiptUpdateRequest(
    val receiptId: UUID,
    val receiptType: ReceiptType,
    val tipsType: TipsType?,
    val tipsPercent: Int?,
    val personCount: Int?
)
