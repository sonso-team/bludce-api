package org.sonso.bludceapi.config

import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

class LobbySocketHandler : TextWebSocketHandler() {

    private val lobbySessions = mutableMapOf<String, MutableList<WebSocketSession>>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val lobbyId = session.uri?.path?.substringAfterLast("/") ?: return
        val sessions = lobbySessions.getOrPut(lobbyId) { mutableListOf() }
        sessions.add(session)

        println("Пользователь подключился к лобби $lobbyId")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val lobbyId = session.uri?.path?.substringAfterLast("/") ?: return
        lobbySessions[lobbyId]?.forEach {
            it.sendMessage(message)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val lobbyId = session.uri?.path?.substringAfterLast("/") ?: return
        lobbySessions[lobbyId]?.remove(session)
    }
}
