package com.ragplatform.rag_backend.service;

import com.ragplatform.rag_backend.dto.QueryRequest;
import com.ragplatform.rag_backend.dto.QueryResponse;
import com.ragplatform.rag_backend.model.DocumentChunk;
import com.ragplatform.rag_backend.repository.DocumentChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class QueryService {
    private static final Logger log = LoggerFactory.getLogger(QueryService.class);

    private final GeminiService geminiService;
    private final DocumentChunkRepository chunkRepository;
    private final CacheService cacheService;

    public QueryService(GeminiService geminiService,
                        DocumentChunkRepository chunkRepository, CacheService cacheService) {
        this.geminiService = geminiService;
        this.chunkRepository = chunkRepository;
        this.cacheService = cacheService;
    }

    public QueryResponse query(QueryRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Processing query: {}", request.getQuestion());

        // Step 1: Check cache first
        String cachedAnswer = cacheService.getCachedAnswer(request.getQuestion());
        if (cachedAnswer != null) {
            log.info("Returning cached answer");
            return new QueryResponse(
                    cachedAnswer,
                    request.getQuestion(),
                    List.of("(from cache)"),
                    System.currentTimeMillis() - startTime
            );
        }

        // Step 2: Convert the question into an embedding
        log.info("Generating question embedding...");
        float[] questionEmbedding = geminiService.generateEmbedding(request.getQuestion());
        String embeddingStr = DocumentChunk.toEmbeddingJson(questionEmbedding);

        // Step 3: Find most similar chunks
        log.info("Searching for similar chunks...");
        int maxChunks = request.getMaxChunks() != null ? request.getMaxChunks() : 5;
        List<DocumentChunk> similarChunks = chunkRepository.findSimilarChunks(
                embeddingStr, maxChunks
        );

        log.info("Found {} similar chunks", similarChunks.size());

        if (similarChunks.isEmpty()) {
            return new QueryResponse(
                    "No relevant documents found. Please upload documents first.",
                    request.getQuestion(),
                    List.of(),
                    System.currentTimeMillis() - startTime
            );
        }

        // Step 4: Build context
        String context = similarChunks.stream()
                .map(DocumentChunk::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));

        // Step 5: Generate answer
        log.info("Generating answer...");
        String answer = geminiService.generateAnswer(context, request.getQuestion());

        // Step 6: Cache the answer for next time
        cacheService.cacheQueryAnswer(request.getQuestion(), answer);

        // Step 7: Return response
        List<String> sourcePreviews = similarChunks.stream()
                .map(c -> c.getContent().substring(0, Math.min(100, c.getContent().length())) + "...")
                .collect(Collectors.toList());

        long latency = System.currentTimeMillis() - startTime;
        log.info("Query completed in {}ms", latency);

        return new QueryResponse(answer, request.getQuestion(), sourcePreviews, latency);
    }
    public Flux<String> queryStream(QueryRequest request) {
        log.info("Processing streaming query: {}", request.getQuestion());

        // Step 1: Convert question to embedding (same as before)
        float[] questionEmbedding = geminiService.generateEmbedding(request.getQuestion());
        String embeddingStr = DocumentChunk.toEmbeddingJson(questionEmbedding);

        // Step 2: Find similar chunks (same as before)
        int maxChunks = request.getMaxChunks() != null ? request.getMaxChunks() : 5;
        List<DocumentChunk> similarChunks = chunkRepository.findSimilarChunks(
                embeddingStr, maxChunks
        );

        if (similarChunks.isEmpty()) {
            return Flux.just("No relevant documents found. Please upload documents first.");
        }

        // Step 3: Build context (same as before)
        String context = similarChunks.stream()
                .map(DocumentChunk::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));

        // Step 4: Return a stream instead of waiting for full answer
        return geminiService.generateAnswerStream(context, request.getQuestion());
    }


}
