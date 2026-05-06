package com.disk.storage.context;

import lombok.Data;

import java.io.InputStream;

@Data
public class StoreFileContext {
    private String filename;
    private Long totalSize;
    private InputStream inputStream;
    /** Populated by the engine after store() completes */
    private String realPath;
}
