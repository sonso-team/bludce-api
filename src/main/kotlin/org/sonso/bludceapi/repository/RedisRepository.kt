package org.sonso.bludceapi.repository

import org.sonso.bludceapi.dto.ws.WSResponse
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

@Repository
class RedisRepository(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private fun key(receiptId: String) = "receipt:$receiptId:positions"

    fun getState(receiptId: String): List<WSResponse> =
        redisTemplate.opsForList().range(key(receiptId), 0, -1)
            ?.filterIsInstance<WSResponse>()
            ?: emptyList()

    fun replaceState(receiptId: String, state: List<WSResponse>) {
        val k = key(receiptId)
        redisTemplate.multi()
        redisTemplate.opsForList().rightPushAll(k, *state.toTypedArray())
        redisTemplate.exec()
    }

    fun clear(receiptId: String) {
        redisTemplate.delete(key(receiptId))
    }
}
