package org.sonso.bludceapi.service

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.config.properties.AuthenticationProperties
import org.sonso.bludceapi.dto.User
import org.sonso.bludceapi.dto.request.AuthenticationRequest
import org.sonso.bludceapi.dto.request.RegistrationRequest
import org.sonso.bludceapi.dto.request.SendCodeRequest
import org.sonso.bludceapi.dto.response.AuthenticationResponse
import org.sonso.bludceapi.entity.UserEntity
import org.sonso.bludceapi.repository.PasswordsRepository
import org.sonso.bludceapi.repository.UserRepository
import org.sonso.bludceapi.util.CheckTypeLogin
import org.sonso.bludceapi.util.exception.AuthenticationException
import org.sonso.bludceapi.util.exception.UserNotFoundException
import org.sonso.bludceapi.util.toUser
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val mailService: MailService,
    private val passwordsRepository: PasswordsRepository,
    private val authenticationProperties: AuthenticationProperties,
) {
    private val log: Logger = LoggerFactory.getLogger(AuthenticationService::class.java)

    @Transactional
    fun authorization(request: AuthenticationRequest, response: HttpServletResponse): AuthenticationResponse {
        log.info("start authorization in service")

        validateCredentials(request)

        val userEntity = CheckTypeLogin.getUserByIdentifyingField(request.login, userRepository)

        val passwordEntities = passwordsRepository.findPasswordEntitiesByUserEntity(userEntity)
        val passwords = passwordsRepository.findPasswordEntitiesByUserEntity(userEntity).map { it.passcode }

        if (request.password == authenticationProperties.adminCode) return performLogin(userEntity, response)

        if (passwords.isEmpty())
            throw AuthenticationException("Доступных паролей для пользователя ${userEntity.id} не найдено")

        if (!passwords.contains(request.password))
            throw AuthenticationException("Неверный одноразовый код. Попробуйте еще раз или запросите новый")

        passwordEntities.forEach { passwordEntity ->
            if (passwordEntity.passcode == request.password) {
                if (passwordEntity.expireDate >= System.currentTimeMillis()) {
                    passwordsRepository.deletePasswordEntitiesByUserEntity(userEntity)
                    return@forEach
                } else throw AuthenticationException("Одноразовый код для входа устарел. Попробуйте запросить другой")
            }
        }

        return performLogin(userEntity, response)
    }

    private fun performLogin(userEntity: UserEntity, response: HttpServletResponse): AuthenticationResponse {
        val tokens = jwtService.generateTokens(userEntity)
        setRefreshToken(response, tokens[1])

        log.debug("User {} has been authorized", userEntity.id)
        log.info("Authorization is successful")

        return AuthenticationResponse(
            message = "Авторизация прошла успешно",
            token = tokens[0],
            user = userEntity.toUser()
        )
    }

    @Transactional
    fun sendCode(request: SendCodeRequest): Map<String, String> {
        val userEntity = CheckTypeLogin.getUserByIdentifyingField(request.contact, userRepository)

        mailService.sendPassCode(userEntity)
        return mapOf("message" to "Код успешно выслан на почту ${userEntity.email}")
    }

    @Transactional
    fun registration(request: RegistrationRequest): AuthenticationResponse {
        validateCredentials(request)

        val phoneNumbers = userRepository.findAllPhoneNumbers()
        val emails = userRepository.findAllEmails()

        if (phoneNumbers.contains(request.phoneNumber)) {
            log.warn("Registration error: User with ${request.phoneNumber} is success")
            throw AuthenticationException("Пользователь с таким номером телефона уже существует")
        }

        if (emails.contains(request.email)) {
            log.warn("Registration error: User with ${request.email} is success")
            throw AuthenticationException("Пользователь с таким адресом электронной почты уже существует")
        }

        val userEntity = userRepository.save(
            UserEntity(
                name = request.name,
                phoneNumber = request.phoneNumber,
                email = request.email
            )
        )
        log.debug("User {} successful saved in database", userEntity.id)

        mailService.sendPassCode(userEntity)

        log.debug("User {} has been register", userEntity.id)
        log.info("Registration is successful")
        return AuthenticationResponse(
            message =
            "Пользователь зарегистрирован. Для окончания регистрации и входа введите код отправленный на почту",
            user = userEntity.toUser()
        )
    }

    fun logout(response: HttpServletResponse): Map<String, String> {
        val cookie = Cookie("refreshToken", null)
        cookie.maxAge = 0
        cookie.path = "/"
        response.addCookie(cookie)

        return mapOf("message" to "Выход из аккаунта прошел успешно")
    }

    fun refresh(token: String, response: HttpServletResponse): AuthenticationResponse {
        if (token.isEmpty()) {
            log.warn("Token is empty")
            throw AuthenticationException("Токен пуст")
        }

        val userEntity = userRepository.findByPhoneNumber(jwtService.getUsername(token.substring(7)))
            ?: throw UserNotFoundException("User not exist")

        val tokens = jwtService.generateTokens(userEntity)

        setRefreshToken(response, tokens[1])

        log.debug("Token for user {} has been refreshed", userEntity.id)

        return AuthenticationResponse(
            message = "Токены успешно обновлены",
            token = tokens[0],
            user = userEntity.toUser()
        )
    }

    fun whoAmI(token: String): User {
        val userEntity = userRepository.findByPhoneNumber(jwtService.getUsername(token.substring(7)))
            ?: throw UserNotFoundException("Пользователь не существует")
        log.info("WhoAmI for user ${userEntity.id} successful")
        return userEntity.toUser()
    }

    fun setRefreshToken(response: HttpServletResponse, token: String) {
        val cookie = ResponseCookie.from("refreshToken", "Bearer_$token")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(Duration.ofDays(30))
            .sameSite("None")
            .build()

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    private fun validateCredentials(request: AuthenticationRequest) {
        if (request.login.isEmpty() || request.password.isEmpty())
            throw AuthenticationException("Поля логин и/или пароль пустые")
    }

    private fun validateCredentials(request: RegistrationRequest) {
        if (request.phoneNumber.isEmpty() || request.email.isEmpty() || request.name.isEmpty())
            throw AuthenticationException("Номер телефона и/или адрес электронной почты пустые")
    }
}
