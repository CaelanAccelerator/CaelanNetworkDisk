package com.disk.storage.context;

import lombok.Data;

import java.util.List;

@Data
public class MergeFileContext {
    private String filename;
    private String identifier;
    private Long userId;
    private List<String> realPathList;

    private String realPath;
}
