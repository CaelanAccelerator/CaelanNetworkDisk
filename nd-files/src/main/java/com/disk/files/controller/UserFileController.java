package com.disk.files.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.disk.base.response.Result;
import com.disk.files.controller.request.CreateFolderRequest;
import com.disk.files.controller.request.DeleteFileRequest;
import com.disk.files.controller.request.MoveFileRequest;
import com.disk.files.controller.request.RenameFileRequest;
import com.disk.files.controller.vo.UserFileVO;
import com.disk.files.domain.context.CreateFolderContext;
import com.disk.files.domain.context.DownloadFileContext;
import com.disk.files.domain.context.MoveFileContext;
import com.disk.files.domain.context.RecycleListContext;
import com.disk.files.domain.context.RecycleRestoreContext;
import com.disk.files.domain.context.RenameFileContext;
import com.disk.files.domain.context.FileChunkMergeContext;
import com.disk.files.domain.context.FileChunkUploadContext;
import com.disk.files.domain.context.FileListContext;
import com.disk.files.domain.context.SecUploadFileContext;
import com.disk.files.domain.context.UploadFileContext;
import com.disk.files.domain.context.UserFileDeleteContext;
import com.disk.files.domain.service.impl.UserFileServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Tag(name = "Files", description = "File and folder management")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class UserFileController {

    private final UserFileServiceImpl userFileService;

    @PostMapping("/folder")
    public Result<Long> createFolder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid CreateFolderRequest req) {

        CreateFolderContext ctx = new CreateFolderContext();
        ctx.setUserId(userId);
        ctx.setFolderName(req.getFolderName());
        ctx.setParentId(req.getParentId());
        return Result.success(userFileService.createFolder(ctx));
    }

    @Operation(hidden = true)
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

    @PostMapping(value = "/file/upload", consumes = "multipart/form-data")
    public Result<Void> upload(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam MultipartFile file,
            @RequestParam String identifier,
            @RequestParam Long parentId,
            @RequestParam Integer fileType) {

        UploadFileContext ctx = new UploadFileContext();
        ctx.setUserId(userId);
        ctx.setFile(file);
        ctx.setIdentifier(identifier);
        ctx.setParentId(parentId);
        ctx.setFileType(fileType);
        userFileService.upload(ctx);
        return Result.success();
    }

    @Operation(hidden = true)
    @PostMapping("/file/chunk-upload")
    public Result<Void> chunkUpload(
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

        userFileService.chunkUpload(ctx);
        return Result.success();
    }

    @Operation(hidden = true)
    @GetMapping("/file/chunk-upload")
    public Result<List<Integer>> getUploadedChunks(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String identifier) {
        return Result.success(userFileService.getUploadedChunks(identifier, userId));
    }

    @GetMapping("/list")
    public Result<IPage<UserFileVO>> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") Long parentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        FileListContext ctx = new FileListContext();
        ctx.setUserId(userId);
        ctx.setParentId(parentId);
        ctx.setPage(page);
        ctx.setSize(size);
        return Result.success(userFileService.listFiles(ctx));
    }

    @GetMapping("/recycle")
    public Result<IPage<UserFileVO>> listRecycle(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        RecycleListContext ctx = new RecycleListContext();
        ctx.setUserId(userId);
        ctx.setPage(page);
        ctx.setSize(size);
        return Result.success(userFileService.listRecycle(ctx));
    }

    @PutMapping("/recycle")
    public Result<Void> restoreRecycle(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid DeleteFileRequest req) {

        RecycleRestoreContext ctx = new RecycleRestoreContext();
        ctx.setUserId(userId);
        ctx.setIds(req.getIds());
        userFileService.restoreRecycle(ctx);
        return Result.success();
    }

    @PutMapping("/file/move")
    public Result<Void> moveFile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid MoveFileRequest req) {

        MoveFileContext ctx = new MoveFileContext();
        ctx.setUserId(userId);
        ctx.setUserFileId(req.getUserFileId());
        ctx.setTargetParentId(req.getTargetParentId());
        userFileService.moveFile(ctx);
        return Result.success();
    }

    @PutMapping("/file/name")
    public Result<Void> renameFile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid RenameFileRequest req) {

        RenameFileContext ctx = new RenameFileContext();
        ctx.setUserId(userId);
        ctx.setUserFileId(req.getUserFileId());
        ctx.setNewFilename(req.getNewFilename());
        userFileService.renameFile(ctx);
        return Result.success();
    }

    @DeleteMapping("/file")
    public Result<Void> deleteFiles(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid DeleteFileRequest req) {

        UserFileDeleteContext ctx = new UserFileDeleteContext();
        ctx.setUserId(userId);
        ctx.setIds(req.getIds());
        userFileService.deleteFiles(ctx);
        return Result.success();
    }

    @GetMapping("/file/download")
    public void download(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long userFileId,
            HttpServletResponse response) throws IOException {

        DownloadFileContext ctx = new DownloadFileContext();
        ctx.setUserId(userId);
        ctx.setUserFileId(userFileId);

        userFileService.validateDownload(ctx);

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\""
                        + URLEncoder.encode(ctx.getFilename(), StandardCharsets.UTF_8)
                        + "\"");

        ctx.setOutputStream(response.getOutputStream());
        userFileService.executeDownload(ctx);
    }

    @Operation(hidden = true)
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
