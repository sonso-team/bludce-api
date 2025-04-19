package org.sonso.bludceapi.repository.jpa

import org.sonso.bludceapi.entity.PasswordEntity
import org.sonso.bludceapi.entity.UserEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PasswordsRepository : CrudRepository<PasswordEntity, UUID> {

    fun findPasswordEntitiesByUserEntity(userEntity: UserEntity): List<PasswordEntity>
    fun deletePasswordEntitiesByUserEntity(userEntity: UserEntity)
}
