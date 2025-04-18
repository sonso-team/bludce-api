package org.sonso.bludceapi.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.dto.ws.InitPayload
import org.sonso.bludceapi.dto.ws.UpdatePayload
import org.sonso.bludceapi.dto.ws.WSResponse
import org.sonso.bludceapi.repository.ReceiptPositionRepository
import org.sonso.bludceapi.repository.RedisRepository
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class LobbySocketHandler(
    private val redis: RedisRepository,
    private val receiptPositionRepository: ReceiptPositionRepository
) : TextWebSocketHandler() {

    private val log = LoggerFactory.getLogger(javaClass)
    private val lobbySessions = ConcurrentHashMap<String, MutableSet<WebSocketSession>>() // чек -> сессии
    private val userIds = ConcurrentHashMap<String, UUID>() // sessionId -> userId
    private val mapper = jacksonObjectMapper()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val lobbyId = lobbyId(session) ?: return
        lobbySessions.computeIfAbsent(lobbyId) { mutableSetOf() }.add(session)

        val uid = UUID.randomUUID()
        userIds[session.id] = uid

        var state = redis.getState(lobbyId)

        if (state.isEmpty()) {
            val positions = receiptPositionRepository.findAllByReceiptId(UUID.fromString(lobbyId))
            state = positions.map {
                WSResponse(
                    id = it.id,
                    name = it.name,
                    quantity = it.quantity,
                    price = it.price,
                    userId = null
                )
            }
            redis.replaceState(lobbyId, state)
            log.debug("[$lobbyId] state загружен из Postgres (${state.size} позиций)")
        } else {
            log.debug("[$lobbyId] state взят из Redis (${state.size} позиций)")
        }

        session.send(InitPayload(userId = uid, state = state))
        log.info("[$lobbyId] user $uid подключился")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val lobbyId = lobbyId(session) ?: return

        val payload: List<WSResponse> = mapper.readValue(
            message.payload,
            object : TypeReference<List<WSResponse>>() {}
        )

        // сохраняем атомарно
        redis.replaceState(lobbyId, payload)

        // разлетается всем, кроме автора
        val update = UpdatePayload(state = payload)
        lobbySessions[lobbyId]?.forEach {
            if (it != session && it.isOpen) it.send(update)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val lobbyId = lobbyId(session) ?: return
        lobbySessions[lobbyId]?.remove(session)
        if (lobbySessions[lobbyId].isNullOrEmpty()) {
            redis.clear(lobbyId)
            lobbySessions.remove(lobbyId)
            log.info("[$lobbyId] лобби закрылось, Redis очищен")
        }
    }

    fun broadcastState(lobbyId: String, state: List<WSResponse>) {
        redis.replaceState(lobbyId, state)

        val update = UpdatePayload(state = state)
        lobbySessions[lobbyId]?.forEach { sess ->
            if (sess.isOpen) sess.send(update)
        }
    }

    private fun WebSocketSession.send(obj: Any) =
        sendMessage(TextMessage(mapper.writeValueAsString(obj)))

    private fun lobbyId(session: WebSocketSession) =
        session.uri?.path?.substringAfterLast("/")
}
