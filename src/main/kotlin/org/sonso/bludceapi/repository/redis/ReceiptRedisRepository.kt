package org.sonso.bludceapi.repository.redis

import org.sonso.bludceapi.dto.ws.WSResponse
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

@Repository
class ReceiptRedisRepository(
    redisTemplate: RedisTemplate<String, Any>
) : RedisRepository<WSResponse>(
    redisTemplate,
    WSResponse::class.java
) {
    override fun key(receiptId: String) = "receipt:$receiptId:positions"
}
