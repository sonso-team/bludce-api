package org.sonso.bludceapi.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.sonso.bludceapi.dto.ReceiptType
import org.sonso.bludceapi.dto.ws.ErrorResponse
import org.sonso.bludceapi.dto.ws.Payload
import org.sonso.bludceapi.dto.ws.UpdateResponse
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

    private data class PathParts(val receiptId: UUID, val uid: UUID?)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        /* 1. Парсим путь */
        val (receiptId, pathUid) = parse(session)
            ?: return session.close(CloseStatus.BAD_DATA.withReason("Некорректный путь"))

        /* 2. Проверяем чек */
        val receipt = receiptRepository.findById(receiptId)
            .orElseGet {
                session.send(ErrorResponse(message = "Чек не найден"))
                session.close(
                    CloseStatus.BAD_DATA.withReason("Receipt $receiptId not found")
                )
                null
            }
            ?: return

        if (receipt.isClosed) {
            session.send(ErrorResponse(message = "Чек уже закрыт"))
            return session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Receipt closed"))
        }

        /* 3. UserId: либо из path (re-connect), либо генерим */
        val uid = pathUid ?: UUID.randomUUID()
        userIds[session.id] = uid

        /* 4. Регистрируем сессию */
        val lobbyKey = receiptId.toString()
        sessions.computeIfAbsent(lobbyKey) { mutableSetOf() }.add(session)

        /* 5. Если юзер новый – кладём его в Redis-«очередь ожидания» */
        if (uid.toString() !in payedUserRedisRepository.getState(lobbyKey))
            payedUserRedisRepository.addUser(lobbyKey, uid)

        /* 6. Получаем id инициатора */
        val initiator = payedUserRedisRepository.getState(lobbyKey).first()

        /* 7. Грузим/кешируем позиции */
        val state = receiptRedisRepository
            .getState(lobbyKey)
            .ifEmpty {
                receiptPositionRepository
                    .findAllByReceiptId(receiptId)
                    .map { it.toWSResponse() }
                    .also { receiptRedisRepository.replaceState(lobbyKey, it) }
            }

        /* 8. Считаем суммы */
        val (paid, full) = sumRecount(state)
        val amount = if (receipt.receiptType == ReceiptType.EVENLY)
            full.divide(BigDecimal(receipt.personCount))
        else paid

        /* 9. Шлём INIT */
        session.send(receipt.toInitPayload(uid, initiator, amount, full, state))
        log.info("User $uid connected to $lobbyKey")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val lobbyId = parse(session)?.receiptId?.toString() ?: return
        val newState: List<Payload> =
            mapper.readValue(message.payload, object : TypeReference<List<Payload>>() {})
        broadcastState(lobbyId, newState)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val receiptId = parse(session)?.receiptId ?: return
        val lobbyKey = receiptId.toString()

        sessions[lobbyKey]?.remove(session)

        if (sessions[lobbyKey].isNullOrEmpty()) {
            /* юзеров-«ожидающих оплаты» больше нет → закрываем чек */
            if (payedUserRedisRepository.getState(lobbyKey).isEmpty()) {
                receiptRepository.findById(receiptId).ifPresent {
                    it.isClosed = true
                    receiptRepository.save(it)
                }
                receiptRedisRepository.clear(lobbyKey)
            }
            sessions.remove(lobbyKey)
            log.info("Lobby $lobbyKey disposed")
        }
    }

    private fun parse(session: WebSocketSession): PathParts? {
        val parts = session.uri?.path
            ?.removePrefix("/ws/lobby/")
            ?.split("/")
            ?.filter { it.isNotBlank() }
            ?: return null // путь пустой/кривой

        // 1. receiptId обязателен
        val receiptId = parts.firstOrNull()
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?: return null

        // 2. uid опционален
        val uid = parts.getOrNull(1)
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }

        return PathParts(receiptId, uid)
    }

    private fun sumRecount(state: List<Payload>): Pair<BigDecimal, BigDecimal> {
        var paid = BigDecimal.ZERO
        var full = BigDecimal.ZERO
        state.forEach {
            val positionTotal = it.price * it.quantity.toBigDecimal()
            full += positionTotal
            if (it.paidBy != null) paid += positionTotal
        }
        return paid to full
    }

    fun broadcastState(lobbyId: String, state: List<Payload>) {
        val update = updateStates(lobbyId, state)
        sessions[lobbyId]?.forEach { if (it.isOpen) it.send(update) }
    }

    private fun updateStates(lobbyId: String, state: List<Payload>): UpdateResponse {
        receiptRedisRepository.replaceState(lobbyId, state)
        val (paid, full) = sumRecount(state)
        return UpdateResponse(
            amount = paid,
            fullAmount = full,
            state = state
        )
    }

    private fun WebSocketSession.send(obj: Any) =
        sendMessage(TextMessage(mapper.writeValueAsString(obj)))
}
