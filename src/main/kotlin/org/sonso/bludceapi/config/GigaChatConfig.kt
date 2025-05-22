package org.sonso.bludceapi.config

import chat.giga.client.GigaChatClient
import chat.giga.client.auth.AuthClient
import chat.giga.client.auth.AuthClientBuilder
import chat.giga.model.Scope
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GigaChatConfig(
    @Value("\${app.gigachat.auth-key}")
    private val authKey: String,
) {

    @Bean
    fun gigaChatClient(): GigaChatClient =
        GigaChatClient.builder()
            .verifySslCerts(false)
            .authClient(
                AuthClient.builder()
                    .withOAuth(
                        AuthClientBuilder.OAuthBuilder.builder()
                            .authKey(authKey)
                            .scope(Scope.GIGACHAT_API_PERS)
                            .build()
                    )
                    .build()
            )
            .build()
}
