package com.disk.files.domain.context;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileChunkUploadContext {
    private String filename;
    private String identifier;
    private Integer chunkNumber;
    private Integer totalChunks;
    private Long currentChunkSize;
    private Long totalSize;
    private Long userId;
    private MultipartFile file;
    /** Set by FileServiceImpl — MinIO object path for this chunk */
    private String realPath;
}
