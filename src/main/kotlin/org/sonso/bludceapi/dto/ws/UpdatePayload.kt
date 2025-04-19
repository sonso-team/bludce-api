package org.sonso.bludceapi.dto.ws

import java.math.BigDecimal

data class UpdatePayload(
    val type: String = "UPDATE",
    val amount: BigDecimal,
    val fullAmount: BigDecimal,
    val state: List<WSResponse>
)
