# CaelanNetworkDisk

A cloud disk backend (Google Drive-style) built with Java microservices, designed to demonstrate production-relevant architecture patterns for the Canadian job market.

## Tech Stack

| Concern | Technology |
|---|---|
| API Gateway + Auth | Spring Cloud Gateway + JWT |
| File Storage | MinIO (S3-compatible) |
| Async Messaging | Kafka + Spring Cloud Stream |
| Inter-service RPC | gRPC + Protobuf |
| ORM | MyBatis-Plus |
| Database | MySQL |
| AI | Claude API (Anthropic) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |

## Key Design Highlights

**Chunked upload with resume support** — clients upload in parts identified by MD5; already-uploaded chunks are skipped on retry, making large file uploads resumable after failure.

**Instant upload (sec-upload) via MD5 deduplication** — if the file content already exists in MinIO, the upload is skipped entirely and only a virtual file record is created, saving storage.

**Two-table file model** — `file` stores physical objects (deduplicated by MD5); `user_file` stores each user's virtual file system entries. Multiple users can reference the same physical object without duplicating storage.

**Soft delete + recycle bin** — deleted files set `del_flag=1` and remain restorable; MinIO objects are never removed, enabling full recycle bin restore.

**JWT at the Gateway** — tokens are validated once at nd-gateway, which injects `X-User-Id` header downstream. Individual services trust the header without re-parsing the token.

**Kafka event pipeline** — every successful upload publishes a `FileUploadedEvent` to Kafka. nd-ai consumes these events, resolves the file path via gRPC, and logs the indexing entry point for the RAG pipeline.

**gRPC server on nd-files** — exposes `GetFileReadInfo` (userFileId → MinIO path + metadata). nd-ai calls this on every Kafka event and also on every summarize request to verify file ownership before reading content.

**AI file summarization** — `POST /api/v1/ai/summarize` accepts one or more file IDs and an optional question. nd-ai reads file content from MinIO and calls Claude to explain or answer questions about the files. Large inputs are handled via map-reduce (per-file summaries combined into one answer). Supports text, markdown, and common code file types.

## API Endpoints

### nd-auth (8090)
| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register |
| POST | `/api/v1/auth/login` | Login, returns JWT |

### nd-files (8082)
| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/files/file/upload` | Single file upload |
| POST | `/api/v1/files/file/chunk-upload` | Upload one chunk |
| GET | `/api/v1/files/file/chunk-upload` | Query uploaded chunks (resume) |
| POST | `/api/v1/files/file/merge` | Merge chunks into final file |
| POST | `/api/v1/files/file/sec-upload` | Instant upload via MD5 dedup |
| GET | `/api/v1/files/file/download` | Download file (streaming) |
| GET | `/api/v1/files/list` | List files with pagination |
| POST | `/api/v1/files/folder` | Create folder |
| PUT | `/api/v1/files/file/name` | Rename file or folder |
| DELETE | `/api/v1/files/file` | Soft delete (move to recycle bin) |
| GET | `/api/v1/files/recycle` | List recycle bin |
| PUT | `/api/v1/files/recycle` | Restore from recycle bin |

### nd-ai (8087)
| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/ai/summarize` | Summarize files with optional question |

All requests go through **nd-gateway (8080)**. Swagger UI: `http://localhost:8082/swagger-ui.html` · `http://localhost:8087/swagger-ui.html`

## Quick Start

```bash
# 1. Start infrastructure
docker compose up -d   # MinIO, Kafka, MySQL

# 2. Run database migrations
# nd-auth/src/main/resources/db/init.sql
# nd-files/src/main/resources/db/init.sql

# 3. Create MinIO bucket "networkdisk" at http://localhost:9001 (minioadmin/minioadmin)

# 4. Build
mvn clean install -DskipTests

# 5. Set environment variable for nd-ai
export CLAUDE_API_KEY=sk-ant-...

# 6. Start services
# nd-auth (8090) → nd-files (8082) → nd-ai (8087) → nd-gateway (8080)
```
