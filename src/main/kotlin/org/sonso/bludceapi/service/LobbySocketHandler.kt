package org.sonso.bludceapi.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.dto.ws.InitPayload
import org.sonso.bludceapi.dto.ws.UpdatePayload
import org.sonso.bludceapi.dto.ws.WSResponse
import org.sonso.bludceapi.repository.RedisRepository
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class LobbySocketHandler(
    private val redis: RedisRepository
) : TextWebSocketHandler() {

    private val log = LoggerFactory.getLogger(javaClass)
    private val lobbySessions = ConcurrentHashMap<String, MutableSet<WebSocketSession>>() // чек → сессии
    private val userIds = ConcurrentHashMap<String, UUID>() // sessionId → userId
    private val mapper = jacksonObjectMapper()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val lobbyId = lobbyId(session) ?: return

        // 1. регаем сессию
        lobbySessions.computeIfAbsent(lobbyId) { mutableSetOf() }.add(session)

        // 2. выдаём юзеру персональный UUID
        val uid = UUID.randomUUID()
        userIds[session.id] = uid

        // 3. шлём INIT + текущее состояние
        val init = InitPayload(userId = uid, state = redis.getState(lobbyId))
        session.send(init)

        log.info("[$lobbyId] user $uid подключился")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val lobbyId = lobbyId(session) ?: return
        val payload = mapper.readValue(message.payload, object : TypeReference<List<WSResponse>>() {})

        // 1. Записываем новое состояние целиком
        redis.replaceState(lobbyId, payload)

        // 2. Рассылаем UPDATE всем, кроме автора
        val update = UpdatePayload(state = payload)
        lobbySessions[lobbyId]?.forEach {
            if (it != session && it.isOpen) it.send(update)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val lobbyId = lobbyId(session) ?: return
        lobbySessions[lobbyId]?.remove(session)

        // последний вышел — чистим Redis
        if (lobbySessions[lobbyId].isNullOrEmpty()) {
            redis.clear(lobbyId)
            lobbySessions.remove(lobbyId)
            log.info("[$lobbyId] лобби закрылось, Redis очищен")
        }
    }

    /** Вспомогалка: отправка любого объекта как JSON */
    private fun WebSocketSession.send(obj: Any) =
        sendMessage(TextMessage(mapper.writeValueAsString(obj)))

    private fun lobbyId(session: WebSocketSession) =
        session.uri?.path?.substringAfterLast("/")
}
