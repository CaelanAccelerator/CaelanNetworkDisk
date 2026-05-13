package com.disk.files.domain.context;

import lombok.Data;

import java.io.OutputStream;

@Data
public class DownloadFileContext {

    private Long userId;
    private Long userFileId;

    private String filename;
    private String realPath;

    private OutputStream outputStream;
}
