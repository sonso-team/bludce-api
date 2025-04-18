package org.sonso.bludceapi.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.sonso.bludceapi.dto.ws.WSResponse
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SessionCallback
import org.springframework.stereotype.Repository

@Repository
class RedisRepository(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private fun key(receiptId: String) = "receipt:$receiptId:positions"

    fun getState(receiptId: String): List<WSResponse> {
        val k = key(receiptId)
        val raw = redisTemplate.opsForList().range(k, 0, -1) ?: return emptyList()

        val mapper = jacksonObjectMapper()
        return raw.mapNotNull {
            when (it) {
                is WSResponse -> it
                is Map<*, *> -> mapper.convertValue(it, WSResponse::class.java)
                else -> null
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun replaceState(receiptId: String, state: List<WSResponse>) {
        val k = key(receiptId)

        redisTemplate.execute(object : SessionCallback<Unit> {
            override fun <K, V> execute(ops: RedisOperations<K, V>): Unit? {
                val stringOps = ops as RedisOperations<String, Any>
                val listOps = stringOps.opsForList()
                stringOps.multi()
                stringOps.delete(k)
                if (state.isNotEmpty()) {
                    listOps.rightPushAll(k, state)
                }
                stringOps.exec()
                return null
            }
        })
    }

    fun clear(receiptId: String) {
        redisTemplate.delete(key(receiptId))
    }
}
