package com.ragplatform.rag_backend.repository;

import com.ragplatform.rag_backend.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, String> {
    // JpaRepository gives us save(), findById(), findAll(), delete() for free
    // We can also define custom queries just by naming methods:
    List<Document> findByStatus(String status);
    List<Document> findByFileType(String fileType);
}
