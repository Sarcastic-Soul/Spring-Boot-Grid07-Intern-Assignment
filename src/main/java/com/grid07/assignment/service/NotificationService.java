package com.grid07.assignment.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.Duration;
import java.util.Set;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final StringRedisTemplate redisTemplate;

    public void handleBotInteraction(UUID botId, UUID userId, String message) {
        String cooldownKey = "user:" + userId.toString() + ":notif_cooldown";
        String listKey = "user:" + userId.toString() + ":pending_notifs";

        Boolean isCooldownActive = redisTemplate.hasKey(cooldownKey);

        if (Boolean.TRUE.equals(isCooldownActive)) {
            redisTemplate.opsForList().rightPush(listKey, message);
        } else {
            log.info("Push Notification Sent to User: {}", userId);
            redisTemplate.opsForValue().set(cooldownKey, "1", Duration.ofMinutes(15));
        }
    }

    @Scheduled(cron = "0 */5 * * * *") // Every 5 minutes
    public void cronSweeper() {
        log.info("Running Notification CRON Sweeper...");
        Set<String> keys = redisTemplate.keys("user:*:pending_notifs");
        if (keys != null) {
            for (String listKey : keys) {
                Long size = redisTemplate.opsForList().size(listKey);
                if (size != null && size > 0) {
                    List<String> messages = redisTemplate.opsForList().range(listKey, 0, -1);
                    if (messages != null && !messages.isEmpty()) {
                        String firstMessage = messages.get(0);
                        log.info("Summarized Push Notification: {} and {} others interacted with your posts.", firstMessage, size - 1);
                    }
                    redisTemplate.delete(listKey);
                }
            }
        }
    }
}
