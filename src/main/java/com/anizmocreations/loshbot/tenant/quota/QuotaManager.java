package com.anizmocreations.loshbot.tenant.quota;

import com.anizmocreations.loshbot.config.ConditionalOnCacheProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@ConditionalOnCacheProvider("REDIS")
public class QuotaManager {

    // LUA Script for atomic Token Bucket implementation in Redis
    private static final String LUA_SCRIPT =
            "local key = KEYS[1] " +
                    "local limit = tonumber(ARGV[1]) " +
                    "local refill_rate = tonumber(ARGV[2]) " +
                    "local now = tonumber(ARGV[3]) " +
                    "local cost = tonumber(ARGV[4]) " +

                    "local bucket = redis.call('hmget', key, 'tokens', 'last_refill') " +
                    "local tokens = tonumber(bucket[1]) or limit " +
                    "local last_refill = tonumber(bucket[2]) or now " +

                    "local delta = math.max(0, now - last_refill) " +
                    "tokens = math.min(limit, tokens + (delta * refill_rate)) " +

                    "if tokens >= cost then " +
                    "  tokens = tokens - cost " +
                    "  redis.call('hmset', key, 'tokens', tokens, 'last_refill', now) " +
                    "  return math.floor(tokens) " +
                    "else " +
                    "  return -1 " +
                    "end";
    private final StringRedisTemplate redisTemplate;

    public QuotaManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Consumes tokens for a specific tenant.
     *
     * @param tenantId   The unique identifier for the tenant.
     * @param limit      The bucket capacity (max tokens).
     * @param refillRate Tokens per second.
     * @param cost       Number of tokens to consume.
     * @return Remaining tokens if successful, -1 if rate-limited.
     */
    public long consume(String tenantId, int limit, double refillRate, int cost) {
        String key = "quota:token_bucket:" + tenantId;
        long now = System.currentTimeMillis() / 1000;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);

        Long result = redisTemplate.execute(
                script,
                Collections.singletonList(key),
                String.valueOf(limit),
                String.valueOf(refillRate),
                String.valueOf(now),
                String.valueOf(cost)
        );

        return result != null ? result : -1;
    }
}
