package com.ragplatform.rag_backend.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class TextExtractorService {

    // Tika is a library that can read PDF, docx, txt and extract plain text
    private final Tika tika = new Tika();

    public String extractText(MultipartFile file) throws IOException, TikaException {
        // tika.parseToString does all the heavy lifting
        // It figures out the file type automatically and extracts text
        String text = tika.parseToString(file.getInputStream());

        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("Could not extract text from file: " + file.getOriginalFilename());
        }

        return text.trim();
    }

    public String getFileType(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null) return "unknown";

        // Get extension from filename
        int dotIndex = originalName.lastIndexOf(".");
        if (dotIndex == -1) return "unknown";

        return originalName.substring(dotIndex + 1).toLowerCase();
    }

}
