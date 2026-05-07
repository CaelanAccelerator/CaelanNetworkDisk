package com.disk.files.domain.context;

import lombok.Data;

@Data
public class FileListContext {
    private Long userId;
    private Long parentId;
    private int page;
    private int size;
}
