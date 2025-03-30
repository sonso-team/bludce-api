package org.sonso.bludceapi.dto.response

import org.sonso.bludceapi.dto.User

data class AuthenticationResponse(
    val message: String? = null,
    val token: String? = null,
    val user: User,
)
