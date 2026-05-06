package com.disk.storage.context;

import lombok.Data;

import java.io.OutputStream;

@Data
public class ReadFileContext {
    private String realPath;
    private OutputStream outputStream;
}
