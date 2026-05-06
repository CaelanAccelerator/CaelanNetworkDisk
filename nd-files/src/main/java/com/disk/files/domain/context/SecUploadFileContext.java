package com.disk.files.domain.context;

import lombok.Data;

@Data
public class SecUploadFileContext {
    private Long userId;
    private String filename;
    private String identifier;
    private Long parentId;
    private Integer fileType;
}
