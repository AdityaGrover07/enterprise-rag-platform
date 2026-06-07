package com.ragplatform.rag_backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DocumentResponse {
    private String id;
    private String name;
    private String fileType;
    private Long fileSize;
    private Integer totalChunks;
    private String status;
    private LocalDateTime uploadedAt;
    private String message;
}
