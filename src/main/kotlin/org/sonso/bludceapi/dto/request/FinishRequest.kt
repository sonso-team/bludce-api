package org.sonso.bludceapi.dto.request

import java.math.BigDecimal

data class FinishRequest(
    val tips: BigDecimal = BigDecimal.ZERO
)
