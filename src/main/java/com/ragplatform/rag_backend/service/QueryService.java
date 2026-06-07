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

    public QueryService(GeminiService geminiService,
                        DocumentChunkRepository chunkRepository) {
        this.geminiService = geminiService;
        this.chunkRepository = chunkRepository;
    }

    public QueryResponse query(QueryRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Processing query: {}", request.getQuestion());

        // Step 1: Convert the question into an embedding
        log.info("Generating question embedding...");
        float[] questionEmbedding = geminiService.generateEmbedding(request.getQuestion());

        // Step 2: Convert float[] to string format for pgvector query
        // pgvector expects format: [0.1,0.2,0.3,...]
        String embeddingStr = DocumentChunk.toEmbeddingJson(questionEmbedding);
        log.info("Searching for similar chunks...");

        // Step 3: Find most similar chunks using vector similarity search
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

        // Step 4: Build context from retrieved chunks
        // We join all chunks into one big context string
        String context = similarChunks.stream()
                .map(DocumentChunk::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));

        log.info("Sending to Gemini for answer generation...");

        // Step 5: Send context + question to Gemini and get answer
        String answer = geminiService.generateAnswer(context, request.getQuestion());

        // Step 6: Collect source previews (first 100 chars of each chunk)
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
