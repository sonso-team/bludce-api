package org.sonso.bludceapi.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.config.properties.AuthenticationProperties
import org.sonso.bludceapi.dto.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*
import java.util.function.Function

@Service
class JwtService(private val authenticationProperties: AuthenticationProperties) {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun extractUsername(token: String): String = extractClaim(token, Claims::getSubject)

    fun <T> extractClaim(token: String, claimsResolver: Function<Claims, T>): T =
        claimsResolver.apply(extractAllClaims(token))

    fun getSingInKey(): Key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(authenticationProperties.token.secret))

    fun generateTokens(userDetails: User): ArrayList<String> {
        log.info("Token generating is began")
        val header = HashMap<String, Any>()
        header["typ"] = "JWT"
        header["alg"] = "HS256"

        val tokens = ArrayList<String>()

        tokens.add(generateAccessToken(header, userDetails))
        tokens.add(generateRefreshToken(header, userDetails))

        return tokens
    }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean = extractExpiration(token).before(Date())

    private fun extractExpiration(token: String): Date = extractClaim(token, Claims::getExpiration)

    private fun generateAccessToken(header: Map<String, Any>, userDetails: UserDetails): String =
        Jwts.builder()
            .setHeader(header)
            .setSubject(userDetails.username)
            .addClaims(HashMap())
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + authenticationProperties.token.accessLifeTime))
            .signWith(getSingInKey(), SignatureAlgorithm.HS256)
            .compact()

    private fun generateRefreshToken(header: Map<String, Any>, userDetails: UserDetails): String =
        Jwts.builder()
            .setHeader(header)
            .setSubject(userDetails.username)
            .addClaims(HashMap())
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + authenticationProperties.token.refreshLifeTime))
            .signWith(getSingInKey(), SignatureAlgorithm.HS256)
            .compact()

    private fun extractAllClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(getSingInKey())
            .build()
            .parseClaimsJws(token.substring("Bearer ".length))
            .body
}
