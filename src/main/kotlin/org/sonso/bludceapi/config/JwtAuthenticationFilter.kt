package org.sonso.bludceapi.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.sonso.bludceapi.service.JwtService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val userDetailsService: UserDetailsService,
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.requestURI.startsWith("/api/auth") && request.requestURI != "/api/auth/who-am-i") {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val token = request.getHeader("Authorization")
            if (token == null || token.isEmpty()) {
                filterChain.doFilter(request, response)
                return
            }

            if (!token.startsWith("Bearer ")) {
                return
            }

            val userEmail = jwtService.extractUsername(token)

            if (userEmail.isNotEmpty() && SecurityContextHolder.getContext().authentication == null) {
                val userDetails = userDetailsService.loadUserByUsername(userEmail)

                if (jwtService.isTokenValid(token, userDetails)) {
                    val authToken = UsernamePasswordAuthenticationToken(userEmail, null, userDetails.authorities)
                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                } else return
            }

            filterChain.doFilter(request, response)
        } catch (ex: Exception) {
            logger.error("Ошибка обработки токена", ex)
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json"
            response.writer.write(
                """
            {
                "status": 401,
                "error": "Unauthorized",
                "message": "Токен не валиден",
                "path": "${request.requestURI}"
            }
                """.trimIndent()
            )
        }
    }
}
