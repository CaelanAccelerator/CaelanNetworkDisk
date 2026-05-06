package com.disk.storage.core;

import com.disk.storage.context.DeleteFileContext;
import com.disk.storage.context.MergeFileContext;
import com.disk.storage.context.ReadFileContext;
import com.disk.storage.context.StoreFileChunkContext;
import com.disk.storage.context.StoreFileContext;

import java.io.IOException;

public interface StorageEngine {

    void store(StoreFileContext context) throws IOException;

    void storeChunk(StoreFileChunkContext context) throws IOException;

    void mergeFile(MergeFileContext context) throws IOException;

    void delete(DeleteFileContext context) throws IOException;

    void read(ReadFileContext context) throws IOException;
}
