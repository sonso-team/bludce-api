package org.sonso.bludceapi.dto.request

data class AuthenticationRequest(
    val login: String,
    val password: String
)
