package org.sonso.bludceapi.util.exception

class UserNotFoundException(message: String = "User not found") : AuthenticationException(message)
