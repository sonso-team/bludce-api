package org.sonso.bludceapi.dto.ws

import java.util.*

data class InitPayload(
    val type: String = "INIT",
    val userId: UUID,
    val state: List<WSResponse>
)
