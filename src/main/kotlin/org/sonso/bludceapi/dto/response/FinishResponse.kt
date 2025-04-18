package org.sonso.bludceapi.dto.response

import java.math.BigDecimal

data class FinishResponse(
    val amount: BigDecimal,
    val tips: BigDecimal,
    val total: BigDecimal
)
