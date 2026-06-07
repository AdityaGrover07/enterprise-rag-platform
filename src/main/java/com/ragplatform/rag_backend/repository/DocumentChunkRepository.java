package com.ragplatform.rag_backend.repository;

import com.ragplatform.rag_backend.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, String> {

    List<DocumentChunk> findByDocumentId(String documentId);

    // Now we cast embeddingJson text to vector for similarity search
    @Query(value = """
        SELECT * FROM document_chunks
        ORDER BY embedding_json::vector <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<DocumentChunk> findSimilarChunks(
            @Param("embedding") String embedding,
            @Param("limit") int limit
    );
}
