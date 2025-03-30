package org.sonso.bludceapi.util

import org.sonso.bludceapi.dto.User
import org.sonso.bludceapi.entity.UserEntity

fun UserEntity.toUser() = User(
    id = this.id.toString(),
    phoneNumber = this.phoneNumber,
    email = this.email,
)
