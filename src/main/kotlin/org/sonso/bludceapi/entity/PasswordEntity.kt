package org.sonso.bludceapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "Passwords")
data class PasswordEntity(
    @Id
    @Column(name = "id", nullable = false, unique = true)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(targetEntity = UserEntity::class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    val userEntity: UserEntity? = null,

    @Column(name = "passcode", nullable = false)
    val passcode: String = "",

    @Column(name = "expire_date", nullable = false)
    val expireDate: Long = 0L,
)
