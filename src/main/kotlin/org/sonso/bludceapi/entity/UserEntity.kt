package org.sonso.bludceapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

@Entity
@Table(name = "Users")
data class UserEntity(
    @Id
    @Column(name = "id", nullable = false, unique = true)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false)
    val name: String = "",

    @Column(name = "email", nullable = false, unique = true)
    val email: String = "",

    @Column(name = "phone_number", nullable = false, unique = true)
    val phoneNumber: String = "",

    @OneToMany(mappedBy = "userEntity")
    val passwords: List<PasswordEntity> = listOf(),
) : UserDetails {
    override fun getUsername() = this.phoneNumber
    override fun getPassword() = this.email
    override fun getAuthorities() = mutableListOf(SimpleGrantedAuthority("USER"))
}
