package com.ragplatform.rag_backend.controller;

import com.ragplatform.rag_backend.repository.DocumentChunkRepository;
import com.ragplatform.rag_backend.repository.DocumentRepository;
import com.ragplatform.rag_backend.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final MetricsService metricsService;

    public HealthController(DocumentRepository documentRepository, DocumentChunkRepository documentChunkRepository, RedisTemplate<String, String> redisTemplate, MetricsService metricsService){
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.redisTemplate = redisTemplate;
        this.metricsService = metricsService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health(){
        Map<String, Object> health = new LinkedHashMap<>();

        String postgresStatus;
        long totalDocuments = 0;

        try {
            totalDocuments = documentRepository.count();
            postgresStatus = "UP";
        } catch (Exception e) {
            postgresStatus = "DOWN: " + e.getMessage();
        }

        String redisStatus;
        try {
            redisTemplate.opsForValue().set("health:ping", "pong");
            String pong = redisTemplate.opsForValue().get("health:ping");
            redisStatus = "pong".equals(pong) ? "UP" : "DOWN";
        } catch (Exception e) {
            redisStatus = "DOWN: " + e.getMessage();
        }

        String overallStatus = postgresStatus.equals("UP") && redisStatus.equals("UP")
                ? "UP" : "DEGRADED";

        health.put("status", overallStatus);
        health.put("postgres", postgresStatus);
        health.put("redis", redisStatus);
        health.put("totalDocuments", totalDocuments);

        return ResponseEntity.ok(health);

    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics(){
        Map<String, Object> metrics = new LinkedHashMap<>();

        long totalDocuments = documentRepository.count();
        long totalChunks = documentChunkRepository.count();

        metrics.put("totalQueries", metricsService.getTotalQueries());
        metrics.put("cacheHits", metricsService.getCacheHits());
        metrics.put("cacheMisses", metricsService.getCacheMisses());
        metrics.put("cacheHitRate", metricsService.getCacheHitRate());
        metrics.put("averageLatencyMs",
                String.format("%.0fms", metricsService.getAverageLatencyMs()));
        metrics.put("embeddingCacheHits", metricsService.getTotalEmbeddingCacheHits());
        metrics.put("totalDocuments", totalDocuments);
        metrics.put("totalChunks", totalChunks);

        return ResponseEntity.ok(metrics);
    }
}
