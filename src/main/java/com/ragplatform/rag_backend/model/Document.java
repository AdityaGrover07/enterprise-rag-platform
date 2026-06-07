package com.ragplatform.rag_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String fileType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private Integer totalChunks;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column(nullable = false)
    private String status;

    public Document() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Integer getTotalChunks() { return totalChunks; }
    public void setTotalChunks(Integer totalChunks) { this.totalChunks = totalChunks; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Manual builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name;
        private String fileType;
        private String content;
        private Long fileSize;
        private Integer totalChunks;
        private LocalDateTime uploadedAt;
        private String status;

        public Builder name(String name) { this.name = name; return this; }
        public Builder fileType(String fileType) { this.fileType = fileType; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder fileSize(Long fileSize) { this.fileSize = fileSize; return this; }
        public Builder totalChunks(Integer totalChunks) { this.totalChunks = totalChunks; return this; }
        public Builder uploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; return this; }
        public Builder status(String status) { this.status = status; return this; }

        public Document build() {
            Document doc = new Document();
            doc.name = this.name;
            doc.fileType = this.fileType;
            doc.content = this.content;
            doc.fileSize = this.fileSize;
            doc.totalChunks = this.totalChunks;
            doc.uploadedAt = this.uploadedAt;
            doc.status = this.status;
            return doc;
        }
    }
}