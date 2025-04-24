package org.sonso.bludceapi.dto.ws

data class ErrorPayload(
    val type: String = "ERROR",
    val message: String,
)
