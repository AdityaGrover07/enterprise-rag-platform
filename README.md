# Enterprise RAG Platform

A production-grade backend platform for semantic document querying using Retrieval Augmented Generation (RAG). Built with Spring Boot, PostgreSQL pgvector, Redis, and Google Gemini AI.

## Architecture



User Query
↓
Spring Boot API
↓
Redis Cache (check first)
↓ (cache miss)
Gemini Embeddings API
↓
pgvector Similarity Search
↓
Top-K Relevant Chunks
↓
Gemini Answer Generation
↓
SSE Streaming Response

## Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| Backend | Spring Boot 4.x | REST API framework |
| Database | PostgreSQL + pgvector | Vector similarity search |
| Cache | Redis | Query and embedding caching |
| AI | Google Gemini API | Embeddings + answer generation |
| Streaming | Server-Sent Events | Real-time response streaming |
| Text Extraction | Apache Tika | PDF/docx/txt parsing |

## Features

- **Document Upload** — Upload PDF, DOCX, TXT files with automatic text extraction and chunking
- **Semantic Search** — Vector similarity search using pgvector cosine distance
- **RAG Pipeline** — Retrieval Augmented Generation for accurate, grounded answers
- **SSE Streaming** — Real-time word-by-word response streaming
- **Redis Caching** — 99.8% latency reduction for repeated queries (3000ms → 6ms)
- **Health Monitoring** — `/api/health` endpoint for dependency status
- **Metrics** — `/api/metrics` for cache hit rate, latency tracking, usage stats

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/documents/upload` | Upload and process a document |
| GET | `/api/documents` | List all documents |
| POST | `/api/query` | Query documents (returns JSON) |
| POST | `/api/query/stream` | Query documents (SSE streaming) |
| GET | `/api/health` | System health check |
| GET | `/api/metrics` | Usage metrics and stats |

## Quick Start

### Prerequisites
- Java 17+
- Docker Desktop

### Run with Docker

```bash
# Start PostgreSQL and Redis
docker run --name rag-postgres \
  -e POSTGRES_USER=raguser \
  -e POSTGRES_PASSWORD=ragpass \
  -e POSTGRES_DB=ragdb \
  -p 5432:5432 -d pgvector/pgvector:pg16

docker run --name rag-redis \
  -p 6379:6379 -d redis:alpine
```

### Configure

Add your Gemini API key to `src/main/resources/application.properties`:
```properties
gemini.api.key=YOUR_GEMINI_API_KEY
```

### Run

```bash
mvn spring-boot:run
```

## Usage Examples

**Upload a document:**
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@/path/to/document.pdf"
```

**Query the document:**
```bash
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d '{"question": "What is the refund policy?"}'
```

**Stream the response:**
```bash
curl -X POST http://localhost:8080/api/query/stream \
  -H "Content-Type: application/json" \
  -d '{"question": "What is the refund policy?"}' \
  --no-buffer
```

**Check health:**
```bash
curl http://localhost:8080/api/health
```

**View metrics:**
```bash
curl http://localhost:8080/api/metrics
```

## Performance

- Average query latency: ~3000ms (first query)
- Cached query latency: ~6ms (repeated queries)
- Cache hit rate: up to 100% for repeated questions
- Embedding dimensions: 768 (Gemini text-embedding-001)
- Chunk size: 500 words with 50 word overlap

## Key Design Decisions

**Why RAG over fine-tuning?**
RAG allows real-time document updates without retraining. New documents are available for querying immediately after upload.

**Why pgvector over Pinecone?**
pgvector keeps the vector store in the same PostgreSQL instance as metadata — simpler ops, ACID transactions, and no additional infrastructure.

**Why chunk with overlap?**
50-word overlap between chunks ensures answers that span chunk boundaries are not missed during retrieval.

**Why Redis for caching?**
Embedding generation is the most expensive operation. Caching embeddings by text hash eliminates redundant Gemini API calls for repeated content.
