package org.sonso.bludceapi.dto.ws

import java.math.BigDecimal

data class UpdateResponse(
    val type: String = "UPDATE",
    val amount: BigDecimal,
    val fullAmount: BigDecimal,
    val payedPersonCount: Int = -1,
    val state: List<Payload>
)
