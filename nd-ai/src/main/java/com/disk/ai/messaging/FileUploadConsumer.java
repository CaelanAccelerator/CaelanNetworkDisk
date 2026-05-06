package com.disk.ai.messaging;

import com.disk.messaging.event.FileUploadedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Kafka consumer — entry point for the RAG indexing pipeline.
 *
 * Current state: stub that logs the event and returns.
 * Phase 2: call nd-files via gRPC to get realPath, download from MinIO,
 *           parse with Apache Tika, chunk, embed, store in pgvector.
 *
 * Supported file types for indexing (others are skipped):
 */
@Slf4j
@Configuration
public class FileUploadConsumer {

    private static final Set<String> INDEXABLE_TYPES = Set.of("pdf", "txt", "docx", "md");

    @Bean
    public Consumer<FileUploadedEvent> fileUploadedConsumer() {
        return event -> {
            log.info("[AI-STUB] file.uploaded — fileId={} userId={} filename={} suffix={}",
                    event.getFileId(), event.getUserId(),
                    event.getFilename(), event.getFileSuffix());

            if (!INDEXABLE_TYPES.contains(event.getFileSuffix())) {
                log.debug("[AI-STUB] Skipping non-indexable file type: {}", event.getFileSuffix());
                return;
            }

            // TODO Phase 2:
            // 1. Call FileGrpcService.getFileReadInfo() to verify access and get realPath
            // 2. Download bytes from MinIO via StorageEngine.read()
            // 3. Parse with Apache Tika → plain text
            // 4. Chunk (size=1200 chars, overlap=200)
            // 5. Call embedding API (OpenAI-compatible, e.g. DashScope)
            // 6. Insert vectors into pgvector table
            log.info("[AI-STUB] RAG indexing pipeline not yet implemented for fileId={}", event.getFileId());
        };
    }
}
