package org.sonso.bludceapi.service

import chat.giga.client.GigaChatClient
import chat.giga.client.auth.AuthClient
import chat.giga.client.auth.AuthClientBuilder
import chat.giga.http.client.HttpClientException
import chat.giga.model.ModelName
import chat.giga.model.Scope
import chat.giga.model.completion.ChatMessage
import chat.giga.model.completion.ChatMessageRole
import chat.giga.model.completion.CompletionRequest
import chat.giga.model.completion.CompletionResponse
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.dto.ReceiptPosition
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GigachatService {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Value("\${app.gigachat.auth-key}")
    private lateinit var authKey: String

    private lateinit var client: GigaChatClient

    @PostConstruct
    fun initClient() {
        client = GigaChatClient.builder()
            .verifySslCerts(false)
            .authClient(
                AuthClient.builder()
                    .withOAuth(
                        AuthClientBuilder.OAuthBuilder.builder()
                            .scope(Scope.GIGACHAT_API_PERS)
                            .authKey(authKey)
                            .build()
                    )
                    .build()
            )
            .build()
    }

    fun correctReceiptPosition(msg: String): List<ReceiptPosition> {
        val res = sendRequest(msg)
        return extractReceiptPositions(res)
    }

    private fun sendRequest(message: String): String {
        var responseGiga: CompletionResponse? = null
        try {
            responseGiga = client.completions(
                CompletionRequest.builder()
                    .model(ModelName.GIGA_CHAT_2)
                    .message(
                        ChatMessage.builder()
                            .content(message)
                            .role(ChatMessageRole.USER)
                            .build()
                    )
                    .build()
            )
        } catch (ex: HttpClientException) {
            println(ex.statusCode().toString() + " " + ex.bodyAsString())
            return "0"
        }

        log.info("GIGACHAT processed the request successfully")
        val result = responseGiga.choices()[0].message().content()

        return result
    }

    private fun extractReceiptPositions(raw: String): List<ReceiptPosition> {
        val start = raw.indexOf('[')
        val end = raw.lastIndexOf(']') + 1
        require(start in 0 until end) { "JSON array not found" }

        var json = raw.substring(start, end)
            .replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
            .lineSequence()
            .map { it.replace(Regex("//.*$"), "") }
            .joinToString("\n")

        val mapper = jacksonObjectMapper()
        mapper.factory.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature())
        mapper.factory.enable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature())

        return mapper.readValue(json)
    }
}
