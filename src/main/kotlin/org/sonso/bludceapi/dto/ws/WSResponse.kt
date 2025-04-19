package org.sonso.bludceapi.dto.ws

import java.math.BigDecimal
import java.util.*

data class WSResponse(
    val id: UUID,
    val name: String,
    val quantity: Int,
    val price: BigDecimal,
    val userId: UUID? = null,
    val paidBy: UUID? = null
)
