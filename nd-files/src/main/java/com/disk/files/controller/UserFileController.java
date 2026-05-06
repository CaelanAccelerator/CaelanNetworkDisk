package com.disk.files.controller;

import com.disk.base.response.Result;
import com.disk.files.domain.context.FileChunkMergeContext;
import com.disk.files.domain.context.FileChunkUploadContext;
import com.disk.files.domain.context.SecUploadFileContext;
import com.disk.files.domain.context.UploadFileContext;
import com.disk.files.domain.service.impl.UserFileServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class UserFileController {

    private final UserFileServiceImpl userFileService;

    /** Step 0: check if file already exists by MD5 — skip upload if true */
    @PostMapping("/file/sec-upload")
    public Result<Map<String, Boolean>> secUpload(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String filename,
            @RequestParam String identifier,
            @RequestParam Long parentId,
            @RequestParam Integer fileType) {

        SecUploadFileContext ctx = new SecUploadFileContext();
        ctx.setUserId(userId);
        ctx.setFilename(filename);
        ctx.setIdentifier(identifier);
        ctx.setParentId(parentId);
        ctx.setFileType(fileType);

        boolean existed = userFileService.secUpload(ctx);
        return Result.success(Map.of("existed", existed));
    }

    /** Single-file upload (small files) */
    @PostMapping("/file/upload")
    public Result<Void> upload(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam MultipartFile file,
            @RequestParam Long parentId,
            @RequestParam Integer fileType) {

        UploadFileContext ctx = new UploadFileContext();
        ctx.setUserId(userId);
        ctx.setFile(file);
        ctx.setParentId(parentId);
        ctx.setFileType(fileType);
        userFileService.upload(ctx);
        return Result.success();
    }

    /** Chunked upload — upload one chunk */
    @PostMapping("/file/chunk-upload")
    public Result<Map<String, Integer>> chunkUpload(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam MultipartFile file,
            @RequestParam String filename,
            @RequestParam String identifier,
            @RequestParam Integer chunkNumber,
            @RequestParam Integer totalChunks,
            @RequestParam Long currentChunkSize,
            @RequestParam Long totalSize) {

        FileChunkUploadContext ctx = new FileChunkUploadContext();
        ctx.setUserId(userId);
        ctx.setFile(file);
        ctx.setFilename(filename);
        ctx.setIdentifier(identifier);
        ctx.setChunkNumber(chunkNumber);
        ctx.setTotalChunks(totalChunks);
        ctx.setCurrentChunkSize(currentChunkSize);
        ctx.setTotalSize(totalSize);

        Integer mergeFlag = userFileService.chunkUpload(ctx);
        return Result.success(Map.of("mergeFlag", mergeFlag));
    }

    /** Query which chunks are already uploaded (for resume support) */
    @GetMapping("/file/chunk-upload")
    public Result<List<Integer>> getUploadedChunks(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String identifier) {
        return Result.success(userFileService.getUploadedChunks(identifier, userId));
    }

    /** Chunked upload — trigger merge after all chunks are uploaded */
    @PostMapping("/file/merge")
    public Result<Void> mergeFile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String filename,
            @RequestParam String identifier,
            @RequestParam Long totalSize,
            @RequestParam Long parentId,
            @RequestParam Integer fileType) {

        FileChunkMergeContext ctx = new FileChunkMergeContext();
        ctx.setUserId(userId);
        ctx.setFilename(filename);
        ctx.setIdentifier(identifier);
        ctx.setTotalSize(totalSize);
        ctx.setParentId(parentId);
        ctx.setFileType(fileType);
        userFileService.mergeFile(ctx);
        return Result.success();
    }
}
