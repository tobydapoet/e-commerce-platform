package com.example.e_commerce.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class RedisTestService {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisTestService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void test() {
        try {
            log.info("🔄 Attempting to set Redis key...");
            redisTemplate.opsForValue().set("test:key", "ok", Duration.ofMinutes(1));

            String value = redisTemplate.opsForValue().get("test:key");
            log.info("✅ Redis OK! Value: {}", value);
            System.out.println("Redis Value: " + value);

        } catch (Exception e) {
            log.error("❌ Redis Error: {}", e.getMessage(), e);
            throw e;
        }
    }
}