package com.ragplatform.rag_backend.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    private static final int CHUNK_SIZE = 500;

    private static final int OVERLAP = 50;

    public List<String> chunkText(String text){

        List<String> chunks = new ArrayList<>();

        // Split into words
        String[] words = text.split("\\s+");

        if (words.length == 0) return chunks;
        int start = 0;

        while (start < words.length) {

            int end = Math.min(start + CHUNK_SIZE, words.length);

            // Join words back into a string for this chunk
            StringBuilder chunk = new StringBuilder();
            for (int i = start; i < end; i++) {
                if (i > start) chunk.append(" ");
                chunk.append(words[i]);
            }

            chunks.add(chunk.toString());

            // Move start forward, but go back OVERLAP words
            // so the next chunk has some context from this one
            start += CHUNK_SIZE - OVERLAP;
        }
        return chunks;
    }

}
