package org.sonso.bludceapi.dto.ws

import java.util.*

data class WSResponse(
    val id: UUID, // id позиции
    val userId: UUID? = null // кто нажал
)
