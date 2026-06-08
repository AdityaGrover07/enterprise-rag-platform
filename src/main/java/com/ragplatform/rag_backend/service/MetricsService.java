package com.ragplatform.rag_backend.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {

    private final AtomicLong totalQueries = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong totalLatencyMs = new AtomicLong(0);
    private final AtomicLong totalEmbeddingCacheHits = new AtomicLong(0);

    public void recordQuery(long latencyMs, boolean fromCache) {
        totalQueries.incrementAndGet();
        totalLatencyMs.addAndGet(latencyMs);
        if (fromCache) {
            cacheHits.incrementAndGet();
        } else {
            cacheMisses.incrementAndGet();
        }
    }

    public void recordEmbeddingCacheHit() {
        totalEmbeddingCacheHits.incrementAndGet();
    }

    public long getTotalQueries() { return totalQueries.get(); }
    public long getCacheHits() { return cacheHits.get(); }
    public long getCacheMisses() { return cacheMisses.get(); }
    public long getTotalEmbeddingCacheHits() { return totalEmbeddingCacheHits.get(); }

    public double getAverageLatencyMs() {
        long queries = totalQueries.get();
        if (queries == 0) return 0;
        return (double) totalLatencyMs.get() / queries;
    }

    public String getCacheHitRate() {
        long total = totalQueries.get();
        if (total == 0) return "0%";
        double rate = (double) cacheHits.get() / total * 100;
        return String.format("%.1f%%", rate);
    }


}
