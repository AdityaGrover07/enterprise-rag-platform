package com.ragplatform.rag_backend.service;

import com.ragplatform.rag_backend.dto.DocumentResponse;
import com.ragplatform.rag_backend.model.Document;
import com.ragplatform.rag_backend.model.DocumentChunk;
import com.ragplatform.rag_backend.repository.DocumentChunkRepository;
import com.ragplatform.rag_backend.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final TextExtractorService textExtractorService;
    private final ChunkingService chunkingService;
    private final GeminiService geminiService;

    // Manual constructor injection
    public DocumentService(
            DocumentRepository documentRepository,
            DocumentChunkRepository chunkRepository,
            TextExtractorService textExtractorService,
            ChunkingService chunkingService,
            GeminiService geminiService) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.textExtractorService = textExtractorService;
        this.chunkingService = chunkingService;
        this.geminiService = geminiService;
    }

    public DocumentResponse uploadDocument(MultipartFile file) {
        log.info("Starting document upload for file: {}", file.getOriginalFilename());

        try {
            log.info("Extracting text...");
            String extractedText = textExtractorService.extractText(file);
            String fileType = textExtractorService.getFileType(file);
            log.info("Extracted {} characters of text", extractedText.length());

            Document document = Document.builder()
                    .name(file.getOriginalFilename())
                    .fileType(fileType)
                    .content(extractedText)
                    .fileSize(file.getSize())
                    .totalChunks(0)
                    .uploadedAt(LocalDateTime.now())
                    .status("PROCESSING")
                    .build();

            document = documentRepository.save(document);
            log.info("Document saved with ID: {}", document.getId());

            log.info("Chunking text...");
            List<String> chunks = chunkingService.chunkText(extractedText);
            log.info("Created {} chunks", chunks.size());

            log.info("Generating embeddings...");
            for (int i = 0; i < chunks.size(); i++) {
                String chunkText = chunks.get(i);
                float[] embedding = geminiService.generateEmbedding(chunkText);

                DocumentChunk chunk = DocumentChunk.builder()
                        .document(document)
                        .content(chunkText)
                        .chunkIndex(i)
                        .embeddingJson(DocumentChunk.toEmbeddingJson(embedding))
                        .build();

                chunkRepository.save(chunk);
                log.info("Saved chunk {}/{}", i + 1, chunks.size());
            }

            document.setStatus("READY");
            document.setTotalChunks(chunks.size());
            documentRepository.save(document);

            log.info("Document processing complete!");

            return DocumentResponse.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .fileType(document.getFileType())
                    .fileSize(document.getFileSize())
                    .totalChunks(chunks.size())
                    .status("READY")
                    .uploadedAt(document.getUploadedAt())
                    .message("Document uploaded and processed successfully!")
                    .build();

        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process document: " + e.getMessage(), e);
        }
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }
}