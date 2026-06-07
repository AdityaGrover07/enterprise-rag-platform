package com.ragplatform.rag_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CacheService {
    private static final Logger log = LoggerFactory.getLogger(CacheService.class);
    // Cache TTL (Time To Live) — how long before cache expires
    private static final Duration QUERY_CACHE_TTL = Duration.ofHours(24);
    private static final Duration EMBEDDING_CACHE_TTL = Duration.ofDays(7);

    private final RedisTemplate<String, String> redisTemplate;

    public CacheService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheQueryAnswer(String question, String answer) {
        String key = "query:" + question.toLowerCase().trim();
        redisTemplate.opsForValue().set(key, answer, QUERY_CACHE_TTL);
        log.debug("Cached answer for question: {}", question);
    }

    public String getCachedAnswer(String question) {
        String key = "query:" + question.toLowerCase().trim();
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.info("Cache HIT for question: {}", question);
        } else {
            log.info("Cache MISS for question: {}", question);
        }
        return cached;
    }

    public void cacheEmbedding(String text, String embeddingJson) {
        // Use hash of text as key to avoid very long keys
        String key = "embedding:" + Math.abs(text.hashCode());
        redisTemplate.opsForValue().set(key, embeddingJson, EMBEDDING_CACHE_TTL);
        log.debug("Cached embedding for text hash: {}", Math.abs(text.hashCode()));
    }

    public String getCachedEmbedding(String text) {
        String key = "embedding:" + Math.abs(text.hashCode());
        return redisTemplate.opsForValue().get(key);
    }

}
