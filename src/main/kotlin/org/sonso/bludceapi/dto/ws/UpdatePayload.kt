package org.sonso.bludceapi.dto.ws

data class UpdatePayload(
    val type: String = "UPDATE",
    val amount: Double,
    val fullAmount: Double,
    val state: List<WSResponse>
)
