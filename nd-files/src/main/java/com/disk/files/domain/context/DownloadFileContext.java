package com.disk.files.domain.context;

import lombok.Data;

import java.io.OutputStream;

@Data
public class DownloadFileContext {
    // --- inputs (set by controller) ---
    private Long userId;
    private Long userFileId;

    // --- populated by validateDownload() ---
    private String filename;
    private String realPath;

    // --- set by controller after validation, before executeDownload() ---
    private OutputStream outputStream;
}