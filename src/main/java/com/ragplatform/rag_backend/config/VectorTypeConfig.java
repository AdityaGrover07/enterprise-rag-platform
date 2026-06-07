package com.ragplatform.rag_backend.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan("com.ragplatform.rag_backend.model")
public class VectorTypeConfig {
    // This ensures our entities are scanned properly
}
