package org.sonso.bludceapi.dto.request

import org.sonso.bludceapi.dto.ReceiptType
import org.sonso.bludceapi.dto.TipsType
import java.util.*

data class ReceiptUpdateRequest(
    val receiptId: UUID,
    val receiptType: ReceiptType,
    val tipsType: TipsType,
    val tipsPercent: Int?,
    val personCount: Int?
)
