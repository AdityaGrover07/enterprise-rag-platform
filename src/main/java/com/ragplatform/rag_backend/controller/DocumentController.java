package com.ragplatform.rag_backend.controller;

import com.ragplatform.rag_backend.dto.DocumentResponse;
import com.ragplatform.rag_backend.model.Document;
import com.ragplatform.rag_backend.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> uploadDocument(@RequestParam("file") MultipartFile file){

        log.info("Received upload request for file: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    DocumentResponse.builder()
                            .message("Please select a file to upload")
                            .build()
            );
        }

        DocumentResponse response = documentService.uploadDocument(file);
        return ResponseEntity.ok(response);

    }

    // GET /api/documents
    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

}
