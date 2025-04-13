package org.sonso.bludceapi.dto

import java.math.BigDecimal

data class ReceiptPosition(
    val name: String,
    val quantity: Int,
    val price: BigDecimal,
)
