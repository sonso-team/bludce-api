package org.sonso.bludceapi.dto.ws

data class UpdatePayload(
    val type: String = "UPDATE",
    val state: List<WSResponse>
)
