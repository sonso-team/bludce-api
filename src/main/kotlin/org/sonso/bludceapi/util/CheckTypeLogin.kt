package org.sonso.bludceapi.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.repository.UserRepository
import org.sonso.bludceapi.service.AuthenticationService
import org.sonso.bludceapi.util.exception.UserNotFoundException

object CheckTypeLogin {

    private val log: Logger = LoggerFactory.getLogger(AuthenticationService::class.java)

    private fun isEmail(login: String) = login.matches(Regex("""^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,6}$"""))
    private fun isPhoneNumber(login: String) = login.matches(Regex("^7\\d{10}$"))

    fun getUserByIdentifyingField(identifierField: String, userRepository: UserRepository) =
        when {
            isPhoneNumber(identifierField) -> {
                log.info("Идентификация пользователя по телефонному номеру")
                log.debug("Идентификация пользователя по телефонному номеру: $identifierField")
                userRepository.findByPhoneNumber(identifierField)
                    ?: throw UserNotFoundException("Пользователь не найден")
            }
            isEmail(identifierField) -> {
                log.info("Идентификация пользователя по email")
                log.debug("Идентификация пользователя по email: $identifierField")
                userRepository.findByEmail(identifierField)
                    ?: throw UserNotFoundException("Пользователь не найден")
            }
            else -> {
                log.warn("Введен неверный формат для идентификации пользователя (не телефонный номер и не почта)")
                throw IllegalArgumentException(
                    "Введен неверный формат для идентификации пользователя" +
                        " (не телефон, не почта)"
                )
            }
        }
}
