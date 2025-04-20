package org.sonso.bludceapi.repository.redis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SessionCallback
import org.springframework.stereotype.Repository

@Repository
class PayedUserRedisRepository(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private fun key(receiptId: String) = "receipt:$receiptId:payed-user"

    fun getState(receiptId: String): List<String> {
        val k = key(receiptId)
        val raw = redisTemplate.opsForList().range(k, 0, -1) ?: return emptyList()

        val mapper = jacksonObjectMapper()
        return raw.mapNotNull {
            try {
                mapper.convertValue(it, String::class.java)
            } catch (e: Exception) {
                logger.warn(e.message)
                null
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun replaceState(receiptId: String, state: List<String>) {
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
