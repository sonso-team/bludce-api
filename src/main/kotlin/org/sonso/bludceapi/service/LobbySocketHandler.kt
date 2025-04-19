package org.sonso.bludceapi.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.dto.ReceiptType
import org.sonso.bludceapi.dto.ws.UpdatePayload
import org.sonso.bludceapi.dto.ws.WSResponse
import org.sonso.bludceapi.entity.ReceiptEntity
import org.sonso.bludceapi.repository.jpa.ReceiptPositionRepository
import org.sonso.bludceapi.repository.jpa.ReceiptRepository
import org.sonso.bludceapi.repository.redis.PayedUserRedisRepository
import org.sonso.bludceapi.repository.redis.ReceiptRedisRepository
import org.sonso.bludceapi.util.toInitPayload
import org.sonso.bludceapi.util.toWSResponse
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
    private val receiptRepository: ReceiptRepository,
    private val receiptRedisRepository: ReceiptRedisRepository,
    private val receiptPositionRepository: ReceiptPositionRepository,
    private val payedUserRedisRepository: PayedUserRedisRepository
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
        var state = receiptRedisRepository.getState(lobbyId)
        if (state.isEmpty()) {
            // 2) если нет — инициализируем из БД
            val entities = receiptPositionRepository.findAllByReceiptId(UUID.fromString(lobbyId))
            state = entities.map { it.toWSResponse() }
            receiptRedisRepository.replaceState(lobbyId, state)
            log.debug("$lobbyId state loaded from Postgres (${state.size} positions)")
        } else {
            log.debug("$lobbyId state loaded from Redis (${state.size} positions)")
        }

        // 3) достаём данные чека (тип, проценты и пр.)
        val receipt: ReceiptEntity = receiptRepository.findById(UUID.fromString(lobbyId)).orElseThrow()

        // 4) считаем fullAmount и amount
        val amounts = sumRecount(state)
        val fullAmount = amounts[1]
        val amount = if (receipt.receiptType == ReceiptType.EVENLY) {
            BigDecimal(
                (fullAmount.toDouble() / receipt.personCount) * payedUserRedisRepository.getState(lobbyId).size
            )
        } else {
            amounts[0]
        }

        // 5) шлём INIT
        session.send(receipt.toInitPayload(uid, amount, fullAmount, state))
        log.info("User $uid connected to $lobbyId")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val lobbyId = lobbyId(session) ?: return

        // читаем обновлённый state из JSON
        val newState: List<WSResponse> = mapper.readValue(
            message.payload,
            object : TypeReference<List<WSResponse>>() {}
        )

        val update = updateStates(lobbyId, newState)
        sessions[lobbyId]?.forEach {
            if (it != session && it.isOpen) it.send(update)
        }
    }

    private fun updateStates(lobbyId: String, state: List<WSResponse>): UpdatePayload {
        // 2) сохраняем в Redis
        receiptRedisRepository.replaceState(lobbyId, state)

        // 3) пересчитываем суммы
        val amounts = sumRecount(state)
        val fullAmount = amounts[1]
        val receipt = receiptRepository.findById(UUID.fromString(lobbyId)).orElseThrow()
        val amount = if (receipt.receiptType == ReceiptType.EVENLY) {
            BigDecimal(
                (fullAmount.toDouble() / receipt.personCount) * payedUserRedisRepository.getState(lobbyId).size
            )
        } else {
            amounts[0]
        }
        log.debug("{} updateState: amount={}, fullAmount={}", lobbyId, amount, fullAmount)

        // 4) рассылаем всем остальным UPDATE
        return UpdatePayload(
            amount = amount,
            fullAmount = fullAmount,
            state = state
        )
    }

    private fun sumRecount(state: List<WSResponse>): List<BigDecimal> {
        val fullAmount = state
            .fold(BigDecimal.ZERO) { acc, p -> acc + p.price.multiply(p.quantity.toBigDecimal()) }
        val amount = state
            .filter { it.paidBy != null }
            .fold(BigDecimal.ZERO) { acc, p -> acc + p.price.multiply(p.quantity.toBigDecimal()) }

        return listOf(amount, fullAmount)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val lobbyId = lobbyId(session) ?: return
        sessions[lobbyId]?.remove(session)
        if (sessions[lobbyId].isNullOrEmpty()) {
            receiptRedisRepository.clear(lobbyId)
            sessions.remove(lobbyId)
            log.info("Lobby $lobbyId has been closed, Redis cleared")
        }
    }

    /**
     * Публичный метод, который освобождает у state.userId,
     * пересчитывает суммы и рассылает всем клиентам UPDATE.
     */
    fun broadcastState(lobbyId: String, state: List<WSResponse>) {
        val update = updateStates(lobbyId, state)
        sessions[lobbyId]?.forEach { sess ->
            if (sess.isOpen) sess.send(update)
        }
    }

    private fun WebSocketSession.send(obj: Any) =
        sendMessage(TextMessage(mapper.writeValueAsString(obj)))

    private fun lobbyId(session: WebSocketSession) =
        session.uri?.path?.substringAfterLast("/")
}
