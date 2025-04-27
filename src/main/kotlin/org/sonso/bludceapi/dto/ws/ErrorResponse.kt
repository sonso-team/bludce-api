package org.sonso.bludceapi.dto.ws

data class ErrorResponse(
    val type: String = "ERROR",
    val message: String,
)
