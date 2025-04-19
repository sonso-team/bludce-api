package org.sonso.bludceapi.repository.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

@Repository
class PayedUserRedisRepository(
    redisTemplate: RedisTemplate<String, Any>
) : RedisRepository<String>(
    redisTemplate,
    String::class.java
) {
    override fun key(receiptId: String) = "receipt:$receiptId:payed-user"
}
