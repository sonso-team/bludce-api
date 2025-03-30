package org.sonso.bludceapi.dto.request

data class RegistrationRequest(
    val name: String,
    val phoneNumber: String,
    val email: String,
)
