package com.ragplatform.rag_backend.service;


import com.ragplatform.rag_backend.model.DocumentChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CacheService cacheService;

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    public GeminiService(WebClient.Builder webClientBuilder, CacheService cacheService){
        this.webClient = webClientBuilder.build();
        this.cacheService = cacheService;
    }

    public float[] generateEmbedding(String text) {
        // Check cache first
        String cached = cacheService.getCachedEmbedding(text);
        if (cached != null) {
            log.info("Embedding cache HIT");
            return DocumentChunk.fromEmbeddingJson(cached);
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "models/gemini-embedding-001",
                    "content", Map.of(
                            "parts", List.of(Map.of("text", text))
                    )
            );

            String response = webClient.post()
                    .uri(apiUrl + "/models/gemini-embedding-001:embedContent?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            JsonNode valuesNode = root.path("embedding").path("values");

            float[] embedding = new float[valuesNode.size()];
            for (int i = 0; i < valuesNode.size(); i++) {
                embedding[i] = (float) valuesNode.get(i).asDouble();
            }

            // Store in cache for next time
            String embeddingJson = DocumentChunk.toEmbeddingJson(embedding);
            cacheService.cacheEmbedding(text, embeddingJson);

            return embedding;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate embedding: " + e.getMessage(), e);
        }
    }


    // Generates an answer from Gemini given context + question
    public String generateAnswer(String context, String question) {
        try {
            String prompt = """
            You are a helpful assistant. Answer the question based ONLY on the context provided below.
            If the answer is not in the context, say "I don't have enough information to answer that."
            
            Context:
            %s
            
            Question: %s
            
            Answer:
            """.formatted(context, question);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    )
            );

            // Retry once on 429
            for (int attempt = 0; attempt < 2; attempt++) {
                try {
                    String response = webClient.post()
                            .uri(apiUrl + "/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                            .header("Content-Type", "application/json")
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    JsonNode root = objectMapper.readTree(response);
                    return root
                            .path("candidates")
                            .get(0)
                            .path("content")
                            .path("parts")
                            .get(0)
                            .path("text")
                            .asText();

                } catch (Exception e) {
                    if (e.getMessage().contains("429") && attempt == 0) {
                        // Wait 10 seconds then retry
                        Thread.sleep(10000);
                    } else {
                        throw e;
                    }
                }
            }
            throw new RuntimeException("Failed after retries");

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate answer: " + e.getMessage(), e);
        }
    }

    public Flux<String> generateAnswerStream(String context, String question) {
        // Get the full answer first (we know this works from our query API)
        String fullAnswer = generateAnswer(context, question);

        // Split into words and stream them one by one
        // This gives the same "typing" effect as real streaming
        String[] words = fullAnswer.split(" ");

        return Flux.fromArray(words)
                .map(word -> word + " ")
                .delayElements(java.time.Duration.ofMillis(50)); // 50ms between words
    }


}
