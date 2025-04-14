package org.sonso.bludceapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.dto.User
import org.sonso.bludceapi.dto.request.AuthenticationRequest
import org.sonso.bludceapi.dto.request.RegistrationRequest
import org.sonso.bludceapi.dto.request.SendCodeRequest
import org.sonso.bludceapi.dto.response.AuthenticationResponse
import org.sonso.bludceapi.service.AuthenticationService
import org.sonso.bludceapi.util.exception.AuthenticationException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
@Tag(
    name = "Аутентификация",
    description = "Основной контроллер аутентификации"
)
class AuthenticationController(
    private val authenticationService: AuthenticationService,
) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/authorization")
    @Operation(summary = "Авторизация пользователя")
    fun authorization(
        @RequestBody request: AuthenticationRequest,
        response: HttpServletResponse,
    ): ResponseEntity<AuthenticationResponse> {
        log.info("Request to authorization")
        return ResponseEntity.ok(authenticationService.authorization(request, response))
    }

    @PostMapping("/send-code")
    @Operation(summary = "Запрос кода на почту")
    fun sendCode(
        @RequestBody request: SendCodeRequest,
    ): ResponseEntity<Map<String, String>> {
        log.info("Request to sending password-code")
        return ResponseEntity.ok(authenticationService.sendCode(request))
    }

    @PostMapping("/registration")
    @Operation(summary = "Регистрация пользователя")
    fun registration(
        @RequestBody request: RegistrationRequest,
    ): ResponseEntity<AuthenticationResponse> {
        log.info("Request to registration")
        return ResponseEntity.ok(authenticationService.registration(request))
    }

    @PutMapping("/logout")
    @Operation(summary = "Выход пользователя с сайта")
    fun logout(
        @CookieValue(value = "refreshToken") token: String,
        response: HttpServletResponse,
    ): ResponseEntity<Map<String, String>> {
        log.info("Request to logout")
        if (token.isEmpty()) throw AuthenticationException("Refresh токен пустой")
        return ResponseEntity.ok(authenticationService.logout(response))
    }

    @GetMapping("/refresh")
    @Operation(summary = "Обновление токена")
    fun refresh(
        @CookieValue(value = "refreshToken") token: String,
        response: HttpServletResponse,
    ): ResponseEntity<AuthenticationResponse> {
        log.info("Request to refresh")
        return ResponseEntity.ok(authenticationService.refresh(token, response))
    }

    @GetMapping("/who-am-i")
    @Operation(description = "Полная инфа об аутентифицированном пользователе")
    fun whoAmI(
        @RequestHeader(value = "Authorization") token: String,
    ): ResponseEntity<User> {
        log.info("Request to WhoAmI")
        return ResponseEntity.ok().body(authenticationService.whoAmI(token))
    }
}
