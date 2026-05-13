package com.disk.ai.messaging;

import com.disk.api.file.FileGrpcServiceGrpc;
import com.disk.api.file.FileReadData;
import com.disk.api.file.FileReadRequest;
import com.disk.api.file.FileReadResponse;
import com.disk.messaging.event.FileUploadedEvent;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.function.Consumer;

@Slf4j
@Configuration
public class FileUploadConsumer {

    private static final Set<String> INDEXABLE_TYPES = Set.of("pdf", "txt", "docx", "md");

    @GrpcClient("nd-files")
    private FileGrpcServiceGrpc.FileGrpcServiceBlockingStub fileGrpcStub;

    @Bean
    public Consumer<FileUploadedEvent> fileUploadedConsumer() {
        return event -> {
            log.info("[AI] file.uploaded — fileId={} userFileId={} userId={} filename={} suffix={}",
                    event.getFileId(), event.getUserFileId(), event.getUserId(),
                    event.getFilename(), event.getFileSuffix());

            if (!INDEXABLE_TYPES.contains(event.getFileSuffix())) {
                log.debug("[AI] Skipping non-indexable file type: {}", event.getFileSuffix());
                return;
            }

            try {
                FileReadRequest request = FileReadRequest.newBuilder()
                        .setUserFileId(event.getUserFileId())
                        .setUserId(event.getUserId())
                        .build();
                FileReadResponse response = fileGrpcStub.getFileReadInfo(request);

                if (!response.getSuccess()) {
                    log.warn("[AI] gRPC denied — userFileId={} reason={}",
                            event.getUserFileId(), response.getMessage());
                    return;
                }

                FileReadData data = response.getData();
                log.info("[AI] gRPC resolved — path={} size={} suffix={}",
                        data.getRealPath(), data.getFileSize(), data.getFileSuffix());

                log.info("[AI] RAG indexing queued for fileId={} (Phase 2)", event.getFileId());

            } catch (Exception e) {
                log.error("[AI] gRPC call failed for fileId={}", event.getFileId(), e);
            }
        };
    }
}
