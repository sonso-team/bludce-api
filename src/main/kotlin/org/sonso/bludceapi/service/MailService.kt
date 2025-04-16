package org.sonso.bludceapi.service

import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.config.properties.AuthenticationProperties
import org.sonso.bludceapi.entity.PasswordEntity
import org.sonso.bludceapi.entity.UserEntity
import org.sonso.bludceapi.repository.PasswordsRepository
import org.springframework.stereotype.Service

@Service
class MailService(
    private val passwordsRepository: PasswordsRepository,
    private val authenticationProperties: AuthenticationProperties,
    private val emailSender: EmailSender
) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    internal fun sendPassCode(userEntity: UserEntity) {
        log.info("Генерация кода подтверждения")
        val passcode = generatePassCode()

        val passwordEntity = PasswordEntity(
            userEntity = userEntity,
            passcode = passcode,
            expireDate = System.currentTimeMillis() + authenticationProperties.passwordLifeTime
        )
        log.info("Код подтверждения для пользователя ${userEntity.id} сгенерирован")
        log.debug("Код подтверждения для пользователя {}: {}", userEntity.id, passcode)
        passwordsRepository.save(passwordEntity)

        emailSender.sendPassCodeMessage(userEntity.email, passcode)
    }

    fun generatePassCode(): String {
        val chars = "0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}
