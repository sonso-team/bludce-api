package org.sonso.bludceapi.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.service.EmailSender
import org.sonso.bludceapi.util.MailGenerator
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
internal class EmailSenderHelper(private val mailSender: JavaMailSenderImpl) : EmailSender {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Async
    override fun sendPassCodeMessage(to: String, passCode: String) {
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
}
