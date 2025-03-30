package org.sonso.bludceapi.util

import org.sonso.bludceapi.dto.User
import org.sonso.bludceapi.entity.UserEntity

fun UserEntity.toUser() = User(
    phoneNumber = this.phoneNumber,
    email = this.email,
)
