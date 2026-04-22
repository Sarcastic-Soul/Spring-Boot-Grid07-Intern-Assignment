package com.grid07.assignment.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisInteractionService {

    private final StringRedisTemplate redisTemplate;

    public void addViralityScore(UUID postId, int score) {
        String key = "post:" + postId.toString() + ":virality_score";
        redisTemplate.opsForValue().increment(key, score);
    }

    public boolean checkAndIncrementBotReply(UUID postId) {
        String key = "post:" + postId.toString() + ":bot_count";
        // Atomic increment
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count > 100) {
            redisTemplate.opsForValue().decrement(key); // rollback
            return false; // Rejected
        }
        return true; // Allowed
    }

    public boolean checkCooldown(UUID botId, UUID humanId) {
        String key = "cooldown:bot_" + botId.toString() + ":human_" + humanId.toString();
        // setIfAbsent returns true if key was set (i.e. wasn't there before), false if it already exists
        Boolean isAllowed = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofMinutes(10));
        return Boolean.TRUE.equals(isAllowed);
    }
}
