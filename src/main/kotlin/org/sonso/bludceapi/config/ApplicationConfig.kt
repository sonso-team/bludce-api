package org.sonso.bludceapi.config

import org.sonso.bludceapi.repository.UserRepository
import org.sonso.bludceapi.util.exception.UserNotFoundException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Configuration
class ApplicationConfig(
    private val userRepository: UserRepository
) {

    @Bean
    fun userDetailsService() = UserDetailsService { phoneNumber: String ->
        userRepository.findByPhoneNumber(phoneNumber) ?: throw UserNotFoundException("Пользователь не существует")
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(): AuthenticationProvider = DaoAuthenticationProvider().apply {
        setUserDetailsService(userDetailsService())
        setPasswordEncoder(passwordEncoder())
    }
}
