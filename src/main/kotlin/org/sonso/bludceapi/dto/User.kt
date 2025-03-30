package org.sonso.bludceapi.dto

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class User(
    val phoneNumber: String,
    val email: String
) : UserDetails {
    override fun getUsername() = this.phoneNumber
    override fun getPassword() = this.email
    override fun getAuthorities() = mutableListOf(SimpleGrantedAuthority("USER"))
}
