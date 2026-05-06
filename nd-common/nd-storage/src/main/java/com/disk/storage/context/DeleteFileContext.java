package com.disk.storage.context;

import lombok.Data;

import java.util.List;

@Data
public class DeleteFileContext {
    private List<String> realPathList;
}
