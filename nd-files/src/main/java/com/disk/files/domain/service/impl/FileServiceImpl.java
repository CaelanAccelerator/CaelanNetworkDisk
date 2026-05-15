package com.disk.files.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disk.base.exception.BizException;
import com.disk.base.utils.IdUtil;
import com.disk.files.domain.context.FileChunkUploadContext;
import com.disk.files.domain.context.SaveFileContext;
import com.disk.files.domain.entity.FileChunkDO;
import com.disk.files.domain.entity.FileDO;
import com.disk.files.infrastructure.mapper.FileChunkMapper;
import com.disk.files.infrastructure.mapper.FileMapper;
import com.disk.storage.context.DeleteFileContext;
import com.disk.storage.context.MergeFileContext;
import com.disk.storage.context.ReadFileContext;
import com.disk.storage.context.StoreFileChunkContext;
import com.disk.storage.context.StoreFileContext;
import com.disk.storage.core.StorageEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileMapper, FileDO> {

    private final StorageEngine storageEngine;
    private final FileChunkMapper fileChunkMapper;

    public void saveFile(SaveFileContext context) {
        storePhysicalFile(context);
        FileDO record = persistFileRecord(context.getFilename(), context.getRealPath(),
                context.getTotalSize(), context.getIdentifier(), context.getUserId());
        context.setFileRecord(record);
    }

    private void storePhysicalFile(SaveFileContext context) {
        StoreFileContext storeCtx = new StoreFileContext();
        storeCtx.setFilename(context.getFilename());
        storeCtx.setTotalSize(context.getTotalSize());
        try {
            storeCtx.setInputStream(context.getFile().getInputStream());
            storageEngine.store(storeCtx);
            context.setRealPath(storeCtx.getRealPath());
        } catch (IOException e) {
            throw new BizException("File upload failed: " + e.getMessage());
        }
    }

    public void saveChunk(FileChunkUploadContext context) {
        StoreFileChunkContext storeCtx = new StoreFileChunkContext();
        storeCtx.setFilename(context.getFilename());
        storeCtx.setIdentifier(context.getIdentifier());
        storeCtx.setChunkNumber(context.getChunkNumber());
        storeCtx.setTotalChunks(context.getTotalChunks());
        storeCtx.setTotalSize(context.getTotalSize());
        storeCtx.setCurrentChunkSize(context.getCurrentChunkSize());
        storeCtx.setUserId(context.getUserId());
        try {
            storeCtx.setInputStream(context.getFile().getInputStream());
            storageEngine.storeChunk(storeCtx);
            context.setRealPath(storeCtx.getRealPath());
        } catch (IOException e) {
            throw new BizException("Chunk upload failed: " + e.getMessage());
        }

        FileChunkDO chunk = new FileChunkDO();
        chunk.setId(IdUtil.get());
        chunk.setIdentifier(context.getIdentifier());
        chunk.setRealPath(context.getRealPath());
        chunk.setChunkNumber(context.getChunkNumber());
        chunk.setCurrentChunkSize(context.getCurrentChunkSize());
        chunk.setTotalSize(context.getTotalSize());
        chunk.setCreateUser(context.getUserId());
        chunk.setCreateTime(new Date());
        chunk.setExpirationTime(Date.from(
                LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
        fileChunkMapper.insert(chunk);
    }

    public List<Integer> getUploadedChunkNumbers(String identifier, Long userId) {
        LambdaQueryWrapper<FileChunkDO> q = Wrappers.lambdaQuery();
        q.eq(FileChunkDO::getIdentifier, identifier)
         .eq(FileChunkDO::getCreateUser, userId)
         .ge(FileChunkDO::getExpirationTime, new Date());
        return fileChunkMapper.selectList(q).stream()
                .map(FileChunkDO::getChunkNumber)
                .collect(Collectors.toList());
    }

    public FileDO mergeChunksAndSave(String filename, String identifier, Long totalSize, Long userId) {
        List<FileChunkDO> chunks = loadValidChunks(identifier, userId);
        List<String> chunkPaths = chunks.stream()
                .sorted(Comparator.comparing(FileChunkDO::getChunkNumber))
                .map(FileChunkDO::getRealPath)
                .collect(Collectors.toList());

        MergeFileContext mergeCtx = new MergeFileContext();
        mergeCtx.setFilename(filename);
        mergeCtx.setIdentifier(identifier);
        mergeCtx.setUserId(userId);
        mergeCtx.setRealPathList(chunkPaths);
        try {
            storageEngine.mergeFile(mergeCtx);
        } catch (IOException e) {
            throw new BizException("Chunk merge failed: " + e.getMessage());
        }

        List<Long> chunkIds = chunks.stream().map(FileChunkDO::getId).collect(Collectors.toList());
        fileChunkMapper.deleteBatchIds(chunkIds);

        return persistFileRecord(filename, mergeCtx.getRealPath(), totalSize, identifier, userId);
    }

    public List<FileDO> findByIdentifier(String identifier, Long userId) {
        LambdaQueryWrapper<FileDO> q = Wrappers.lambdaQuery();
        q.eq(FileDO::getIdentifier, identifier).eq(FileDO::getCreateUser, userId);
        return list(q);
    }

    private List<FileChunkDO> loadValidChunks(String identifier, Long userId) {
        LambdaQueryWrapper<FileChunkDO> q = Wrappers.lambdaQuery();
        q.eq(FileChunkDO::getIdentifier, identifier)
         .eq(FileChunkDO::getCreateUser, userId)
         .ge(FileChunkDO::getExpirationTime, new Date());
        List<FileChunkDO> chunks = fileChunkMapper.selectList(q);
        if (chunks.isEmpty()) {
            throw new BizException("No valid chunks found for identifier: " + identifier);
        }
        return chunks;
    }

    private FileDO persistFileRecord(String filename, String realPath,
                                     Long totalSize, String identifier, Long userId) {
        FileDO record = new FileDO();
        record.setId(IdUtil.get());
        record.setFilename(filename);
        record.setRealPath(realPath);
        record.setFileSize(totalSize);
        record.setFileSuffix(extractSuffix(filename));
        record.setIdentifier(identifier);
        record.setCreateUser(userId);
        record.setCreateTime(new Date());

        if (!save(record)) {
            DeleteFileContext deleteCtx = new DeleteFileContext();
            deleteCtx.setRealPathList(Collections.singletonList(realPath));
            try {
                storageEngine.delete(deleteCtx);
            } catch (IOException rollbackEx) {
                log.error("Rollback failed — orphaned object at {}", realPath, rollbackEx);

            }
            throw new BizException("Failed to persist file record");
        }
        return record;
    }

    public void readFile(String realPath, OutputStream out) {
        ReadFileContext ctx = new ReadFileContext();
        ctx.setRealPath(realPath);
        ctx.setOutputStream(out);
        try {
            storageEngine.read(ctx);
        } catch (IOException e) {
            throw new BizException("File read failed: " + e.getMessage());
        }
    }

    public void deletePhysicalFile(Long fileId) {
        FileDO file = getById(fileId);
        if (file == null) return;

        DeleteFileContext ctx = new DeleteFileContext();
        ctx.setRealPathList(Collections.singletonList(file.getRealPath()));
        try {
            storageEngine.delete(ctx);
        } catch (IOException e) {
            log.warn("[RecycleCleanup] Failed to delete from storage: {}", file.getRealPath(), e);
        }
        removeById(fileId);
    }

    private String extractSuffix(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "";
    }
}
