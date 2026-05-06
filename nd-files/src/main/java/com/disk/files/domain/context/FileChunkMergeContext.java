package com.disk.files.domain.context;

import lombok.Data;

@Data
public class FileChunkMergeContext {
    private String filename;
    private String identifier;
    private Long totalSize;
    private Long userId;
    private Integer fileType;
    private Long parentId;
}
