package org.sonso.bludceapi.repository.redis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SessionCallback
import org.springframework.stereotype.Repository

@Repository
class ReceiptInitiatorRedisRepository(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private fun key(receiptId: String) = "receipt:$receiptId:initiator"

    fun getState(receiptId: String): String {
        val k = key(receiptId)
        val raw = redisTemplate.opsForList().range(k, 0, -1) ?: return ""

        val mapper = jacksonObjectMapper()
        val value = raw.mapNotNull {
            try {
                mapper.convertValue(it, String::class.java)
            } catch (e: Exception) {
                logger.warn(e.message)
                null
            }
        }
        return value.first()
    }

    @Suppress("UNCHECKED_CAST")
    fun replaceState(receiptId: String, initiatorId: String) {
        val k = key(receiptId)

        val listForRedis = listOf(initiatorId)

        redisTemplate.execute(object : SessionCallback<Unit> {
            override fun <K, V> execute(ops: RedisOperations<K, V>): Unit? {
                val stringOps = ops as RedisOperations<String, Any>
                val listOps = stringOps.opsForList()
                stringOps.multi()
                stringOps.delete(k)
                listOps.rightPush(k, listForRedis)
                stringOps.exec()
                return null
            }
        })
    }

    fun clear(receiptId: String) {
        redisTemplate.delete(key(receiptId))
    }
}
