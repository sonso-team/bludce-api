package org.sonso.bludceapi.dto.response

import java.math.BigDecimal
import java.util.*

data class ReceiptPositionResponse(
    val name: String,
    val quantity: Int,
    val price: BigDecimal,
    val receiptId: UUID?
)
