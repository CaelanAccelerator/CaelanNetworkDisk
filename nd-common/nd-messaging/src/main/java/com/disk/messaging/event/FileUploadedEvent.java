package com.disk.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Published to Kafka topic "file.uploaded" after a file is successfully stored and
 * its metadata record is committed to MySQL.
 * nd-ai consumes this to trigger the RAG indexing pipeline.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadedEvent {
    private Long fileId;
    private Long userFileId;
    private Long userId;
    private String filename;
    private String realPath;
    private String fileSuffix;
    private Long fileSize;
}
