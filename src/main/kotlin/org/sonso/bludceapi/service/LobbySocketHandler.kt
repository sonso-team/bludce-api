package org.sonso.bludceapi.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.dto.ws.InitPayload
import org.sonso.bludceapi.dto.ws.UpdatePayload
import org.sonso.bludceapi.dto.ws.WSResponse
import org.sonso.bludceapi.entity.ReceiptEntity
import org.sonso.bludceapi.repository.ReceiptPositionRepository
import org.sonso.bludceapi.repository.ReceiptRepository
import org.sonso.bludceapi.repository.RedisRepository
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class LobbySocketHandler(
    private val redis: RedisRepository,
    private val receiptRepo: ReceiptRepository,
    private val positionRepo: ReceiptPositionRepository
) : TextWebSocketHandler() {

    private val log = LoggerFactory.getLogger(javaClass)
    private val sessions = ConcurrentHashMap<String, MutableSet<WebSocketSession>>()
    private val userIds = ConcurrentHashMap<String, UUID>()
    private val mapper = jacksonObjectMapper()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val lobbyId = lobbyId(session) ?: return
        sessions.computeIfAbsent(lobbyId) { mutableSetOf() }.add(session)

        // выдаём каждому свой userId
        val uid = UUID.randomUUID()
        userIds[session.id] = uid

        // 1) вытаскиваем текущий state из Redis
        var state = redis.getState(lobbyId)
        if (state.isEmpty()) {
            // 2) если нет — инициализируем из БД
            val entities = positionRepo.findAllByReceiptId(UUID.fromString(lobbyId))
            state = entities.map {
                WSResponse(
                    id = it.id,
                    name = it.name,
                    quantity = it.quantity,
                    price = it.price,
                    userId = null,
                    paidBy = null
                )
            }
            redis.replaceState(lobbyId, state)
            log.debug("[$lobbyId] state загружен из Postgres (${state.size} позиций)")
        } else {
            log.debug("[$lobbyId] state взят из Redis (${state.size} позиций)")
        }

        // 3) достаём данные чека (тип, проценты и пр.)
        val receipt: ReceiptEntity = receiptRepo.findById(UUID.fromString(lobbyId)).orElseThrow()

        // 4) считаем fullAmount и amount
        val fullAmount = state
            .fold(BigDecimal.ZERO) { acc, p -> acc + p.price.multiply(p.quantity.toBigDecimal()) }
            .toDouble()
        val amount = state
            .filter { it.paidBy != null }
            .fold(BigDecimal.ZERO) { acc, p -> acc + p.price.multiply(p.quantity.toBigDecimal()) }
            .toDouble()

        // 5) шлём INIT
        val init = InitPayload(
            userId = uid,
            receiptType = receipt.receiptType,
            tipsType = receipt.tipsType,
            amount = amount,
            fullAmount = fullAmount,
            state = state
        )
        session.send(init)
        log.info("[$lobbyId] user $uid подключился")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val lobbyId = lobbyId(session) ?: return

        // 1) читаем обновлённый state из JSON
        val newState: List<WSResponse> = mapper.readValue(
            message.payload,
            object : TypeReference<List<WSResponse>>() {}
        )
        // 2) сохраняем в Redis
        redis.replaceState(lobbyId, newState)

        // 3) пересчитываем суммы
        val fullAmount = newState
            .fold(BigDecimal.ZERO) { acc, p -> acc + p.price.multiply(p.quantity.toBigDecimal()) }
            .toDouble()
        val amount = newState
            .filter { it.paidBy != null }
            .fold(BigDecimal.ZERO) { acc, p -> acc + p.price.multiply(p.quantity.toBigDecimal()) }
            .toDouble()

        // 4) рассылаем всем остальным UPDATE
        val update = UpdatePayload(
            amount = amount,
            fullAmount = fullAmount,
            state = newState
        )
        sessions[lobbyId]?.forEach {
            if (it != session && it.isOpen) it.send(update)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val lobbyId = lobbyId(session) ?: return
        sessions[lobbyId]?.remove(session)
        if (sessions[lobbyId].isNullOrEmpty()) {
            redis.clear(lobbyId)
            sessions.remove(lobbyId)
            log.info("[$lobbyId] лобби закрылось, Redis очищен")
        }
    }

    private fun WebSocketSession.send(obj: Any) =
        sendMessage(TextMessage(mapper.writeValueAsString(obj)))

    private fun lobbyId(session: WebSocketSession) =
        session.uri?.path?.substringAfterLast("/")
}
