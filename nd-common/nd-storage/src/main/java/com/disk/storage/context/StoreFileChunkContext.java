package com.disk.storage.context;

import lombok.Data;

import java.io.InputStream;

@Data
public class StoreFileChunkContext {
    private String filename;
    private String identifier;
    private Long totalSize;
    private Long currentChunkSize;
    private Integer chunkNumber;
    private Integer totalChunks;
    private Long userId;
    private InputStream inputStream;

    private String realPath;
}
