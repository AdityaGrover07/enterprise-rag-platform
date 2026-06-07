package com.ragplatform.rag_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer chunkIndex;

    @Column(columnDefinition = "TEXT")
    private String embeddingJson;

    // Empty constructor - required by JPA
    public DocumentChunk() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; }

    public String getEmbeddingJson() { return embeddingJson; }
    public void setEmbeddingJson(String embeddingJson) { this.embeddingJson = embeddingJson; }

    // Builder pattern implemented manually
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Document document;
        private String content;
        private Integer chunkIndex;
        private String embeddingJson;

        public Builder document(Document document) {
            this.document = document;
            return this;
        }
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        public Builder chunkIndex(Integer chunkIndex) {
            this.chunkIndex = chunkIndex;
            return this;
        }
        public Builder embeddingJson(String embeddingJson) {
            this.embeddingJson = embeddingJson;
            return this;
        }
        public DocumentChunk build() {
            DocumentChunk chunk = new DocumentChunk();
            chunk.document = this.document;
            chunk.content = this.content;
            chunk.chunkIndex = this.chunkIndex;
            chunk.embeddingJson = this.embeddingJson;
            return chunk;
        }
    }

    // Helpers for embedding conversion
    public static String toEmbeddingJson(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    public static float[] fromEmbeddingJson(String json) {
        String cleaned = json.replace("[", "").replace("]", "");
        String[] parts = cleaned.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }
}