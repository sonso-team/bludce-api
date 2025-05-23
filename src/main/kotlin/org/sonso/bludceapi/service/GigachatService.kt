package org.sonso.bludceapi.service

import chat.giga.client.GigaChatClient
import chat.giga.model.ModelName
import chat.giga.model.completion.ChatMessage
import chat.giga.model.completion.ChatMessageRole
import chat.giga.model.completion.CompletionRequest
import chat.giga.model.completion.CompletionResponse
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.dto.ReceiptPosition
import org.springframework.stereotype.Service

@Service
class GigachatService(
    private val client: GigaChatClient,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun correctReceiptPosition(msg: String): List<ReceiptPosition> = extractReceiptPositions(
        sendRequest(msg)
    )

    private fun sendRequest(message: String): String {
        val responseGiga: CompletionResponse = client.completions(
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

        log.info("GIGACHAT processed the request successfully")
        val result = responseGiga
            .choices()
            .first()
            .message()
            .content()

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
