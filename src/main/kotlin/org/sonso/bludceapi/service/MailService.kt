package org.sonso.bludceapi.service

import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.config.properties.AuthenticationProperties
import org.sonso.bludceapi.entity.PasswordEntity
import org.sonso.bludceapi.entity.UserEntity
import org.sonso.bludceapi.repository.PasswordsRepository
import org.sonso.bludceapi.util.MailGenerator
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class MailService(
    private val passwordsRepository: PasswordsRepository,
    private val authenticationProperties: AuthenticationProperties,
    private val mailSender: JavaMailSenderImpl
) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun sendPassCode(userEntity: UserEntity) {
        log.info("Generation verification code")
        val passcode = generatePassCode()

        val passwordEntity = PasswordEntity(
            userEntity = userEntity,
            passcode = passcode,
            expireDate = System.currentTimeMillis() + authenticationProperties.passwordLifeTime
        )
        log.info("Verification code for user ${userEntity.id} has been generated")
        log.debug("Verification code for user {}: {}", userEntity.id, passcode)
        passwordsRepository.save(passwordEntity)

        sendPassCodeMessage(userEntity.email, passcode)
    }

    private fun sendPassCodeMessage(to: String, passCode: String) {
        log.info("Sending mail to verify is began")

        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom("no-reply@gmail.com")
        helper.setTo(to)
        helper.setSubject("$passCode - одноразовый код для входа")
        helper.setText(MailGenerator.passwordMailTemplate(passCode), true) // true for HTML

        mailSender.send(message)
        log.info("Passcode mail has been sent")
        log.debug("Passcode mail to $to has been sent")
    }

    fun generatePassCode(): String {
        val chars = "0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}
