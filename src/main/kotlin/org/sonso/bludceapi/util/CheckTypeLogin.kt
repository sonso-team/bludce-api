package org.sonso.bludceapi.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.repository.jpa.UserRepository
import org.sonso.bludceapi.service.AuthenticationService
import org.sonso.bludceapi.util.exception.UserNotFoundException

object CheckTypeLogin {

    private val log: Logger = LoggerFactory.getLogger(AuthenticationService::class.java)

    private fun isEmail(login: String) = login.matches(Regex("""^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,6}$"""))
    fun isPhoneNumber(login: String) = login.matches(Regex("^7\\d{10}$"))

    fun getUserByIdentifyingField(identifierField: String, userRepository: UserRepository) =
        when {
            isPhoneNumber(identifierField) -> {
                log.info("Identify user by phone number")
                log.debug("Identify user by phone number: $identifierField")
                userRepository.findByPhoneNumber(identifierField)
                    ?: throw UserNotFoundException("Пользователь не найден")
            }
            isEmail(identifierField) -> {
                log.info("Identify user by email")
                log.debug("Identify user by email: $identifierField")
                userRepository.findByEmail(identifierField)
                    ?: throw UserNotFoundException("Пользователь не найден")
            }
            else -> {
                log.warn("Invalid user identify format. Expected phone number or email")
                throw IllegalArgumentException(
                    "Invalid user identify format. Expected phone number or email"
                )
            }
        }
}
