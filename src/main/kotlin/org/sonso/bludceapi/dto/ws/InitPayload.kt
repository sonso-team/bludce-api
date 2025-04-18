package org.sonso.bludceapi.dto.ws

import org.sonso.bludceapi.dto.ReceiptType
import org.sonso.bludceapi.dto.TipsType
import java.util.*

data class InitPayload(
    val type: String = "INIT",
    val userId: UUID,
    val receiptType: ReceiptType,
    val tipsType: TipsType,
    val amount: Double,
    val fullAmount: Double,
    val state: List<WSResponse>
)
