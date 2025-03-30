package org.sonso.bludceapi.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.config.properties.AuthenticationProperties
import org.sonso.bludceapi.entity.UserEntity
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtService(
    private val authenticationProperties: AuthenticationProperties,
) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    private val signKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(authenticationProperties.token.secret))

    // Генератор токенов
    fun generateTokens(userDetails: UserEntity): ArrayList<String> {
        log.info("Token generating is began")
        val tokens = ArrayList<String>()

        tokens.add(generateAccessToken(userDetails))
        tokens.add(generateRefreshToken(userDetails))

        return tokens
    }

    private fun generateAccessToken(user: UserEntity): String =
        Jwts.builder()
            .subject(user.username)
            .claim("id", user.id)
            .expiration(Date(System.currentTimeMillis() + authenticationProperties.token.accessLifeTime))
            .signWith(signKey)
            .compact()

    private fun generateRefreshToken(user: UserEntity): String =
        Jwts.builder()
            .subject(user.username)
            .claim("id", user.id)
            .expiration(Date(System.currentTimeMillis() + authenticationProperties.token.refreshLifeTime))
            .signWith(signKey)
            .compact()

    // Валидаторы
    private fun isTokenExpired(token: String): Boolean = extractClaims(token).expiration.before(Date())

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractClaims(token).subject
        return username == userDetails.username && !isTokenExpired(token)
    }

    // Экстракторы
    fun getUsername(token: String): String = extractClaims(token).subject

    private fun extractClaims(token: String): Claims = Jwts
        .parser()
        .verifyWith(signKey)
        .build()
        .parseSignedClaims(token)
        .payload
}
