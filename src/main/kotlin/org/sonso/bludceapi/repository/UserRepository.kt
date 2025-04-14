package org.sonso.bludceapi.repository

import org.sonso.bludceapi.entity.UserEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : CrudRepository<UserEntity, UUID> {
    fun findByPhoneNumber(phoneNumber: String): UserEntity?

    fun findByEmail(email: String): UserEntity?

    @Query("select u.phoneNumber from UserEntity u")
    fun findAllPhoneNumbers(): List<String>

    @Query("select u.email from UserEntity u")
    fun findAllEmails(): List<String>
}
