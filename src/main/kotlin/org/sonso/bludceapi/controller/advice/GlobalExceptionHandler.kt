package org.sonso.bludceapi.controller.advice

import io.swagger.v3.oas.annotations.Hidden
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.util.exception.AuthenticationException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingRequestCookieException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Hidden
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(MissingRequestCookieException::class)
    fun handleMissingCookie(
        ex: MissingRequestCookieException,
    ): ResponseEntity<Map<String, String>> {
        log.warn("MissingRequestCookieException: ${ex.message}")
        val response = mapOf("message" to "Refresh токен отсутствует. Пожалуйста, повторите процедуру входа заново")
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(
        ex: AuthenticationException,
    ): ResponseEntity<Map<String, String?>> {
        log.warn("AuthenticationException: ${ex.message}")
        val response = mapOf("message" to ex.message)
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
    ): ResponseEntity<Map<String, String>> {
        log.error("Unhandled exception: ${ex.message}", ex)
        log.debug(ex.stackTraceToString())
        val response = mapOf("message" to (ex.message ?: "Неизвестная ошибка"))
        return ResponseEntity.badRequest().body(response)
    }
}
