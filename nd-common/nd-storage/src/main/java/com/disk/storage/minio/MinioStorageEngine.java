package com.disk.storage.minio;

import com.disk.base.exception.BizException;
import com.disk.storage.context.DeleteFileContext;
import com.disk.storage.context.MergeFileContext;
import com.disk.storage.context.ReadFileContext;
import com.disk.storage.context.StoreFileChunkContext;
import com.disk.storage.context.StoreFileContext;
import com.disk.storage.core.StorageEngine;
import io.minio.ComposeObjectArgs;
import io.minio.ComposeSource;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioStorageEngine implements StorageEngine {

    private final MinioClient minioClient;
    private final MinioStorageConfig config;

    @Override
    public void store(StoreFileContext context) throws IOException {
        String objectName = "files/" + UUID.randomUUID() + "/" + context.getFilename();
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(config.getBucketName())
                            .object(objectName)
                            .stream(context.getInputStream(), context.getTotalSize(), -1)
                            .build()
            );
            context.setRealPath(objectName);
        } catch (Exception e) {
            throw new IOException("MinIO store failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void storeChunk(StoreFileChunkContext context) throws IOException {
        // Chunk path: chunks/{identifier}/{chunkNumber}
        // Each chunk must be ≥ 5 MB except the last (MinIO compose constraint).
        String objectName = "chunks/" + context.getIdentifier() + "/" + context.getChunkNumber();
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(config.getBucketName())
                            .object(objectName)
                            .stream(context.getInputStream(), context.getCurrentChunkSize(), -1)
                            .build()
            );
            context.setRealPath(objectName);
        } catch (Exception e) {
            throw new IOException("MinIO storeChunk failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void mergeFile(MergeFileContext context) throws IOException {
        List<ComposeSource> sources = context.getRealPathList().stream()
                .map(p -> ComposeSource.builder()
                        .bucket(config.getBucketName())
                        .object(p)
                        .build())
                .toList();

        String targetObject = "files/" + context.getIdentifier() + "/" + context.getFilename();
        try {
            minioClient.composeObject(
                    ComposeObjectArgs.builder()
                            .bucket(config.getBucketName())
                            .object(targetObject)
                            .sources(sources)
                            .build()
            );
            context.setRealPath(targetObject);
            deleteObjects(context.getRealPathList());
        } catch (Exception e) {
            throw new IOException("MinIO mergeFile failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(DeleteFileContext context) throws IOException {
        deleteObjects(context.getRealPathList());
    }

    @Override
    public void read(ReadFileContext context) throws IOException {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(config.getBucketName())
                        .object(context.getRealPath())
                        .build())) {
            stream.transferTo(context.getOutputStream());
        } catch (Exception e) {
            throw new IOException("MinIO read failed: " + e.getMessage(), e);
        }
    }

    private void deleteObjects(List<String> paths) {
        paths.forEach(path -> {
            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(config.getBucketName())
                                .object(path)
                                .build()
                );
            } catch (Exception e) {
                log.error("Failed to delete MinIO object: {}", path, e);
            }
        });
    }
}
