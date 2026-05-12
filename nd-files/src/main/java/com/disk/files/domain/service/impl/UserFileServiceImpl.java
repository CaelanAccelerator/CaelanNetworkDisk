package com.disk.files.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disk.base.exception.BizException;
import com.disk.base.utils.IdUtil;
import com.disk.files.controller.vo.UserFileVO;
import com.disk.files.domain.context.DownloadFileContext;
import com.disk.files.domain.context.FileChunkMergeContext;
import com.disk.files.domain.context.FileChunkUploadContext;
import com.disk.files.domain.context.FileListContext;
import com.disk.files.domain.context.SaveFileContext;
import com.disk.files.domain.context.SecUploadFileContext;
import com.disk.files.domain.context.UploadFileContext;
import com.disk.files.domain.context.UserFileDeleteContext;
import com.disk.files.domain.entity.FileDO;
import com.disk.files.domain.entity.UserFileDO;
import com.disk.files.infrastructure.mapper.UserFileMapper;
import com.disk.messaging.event.FileUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFileServiceImpl extends ServiceImpl<UserFileMapper, UserFileDO> {

    private final FileServiceImpl fileService;
    private final StreamBridge streamBridge;

    // -------------------------------------------------------------------------
    // Instant (sec) upload — returns false if the file has never been seen before
    // -------------------------------------------------------------------------

    public boolean secUpload(SecUploadFileContext context) {
        List<FileDO> existing = fileService.findByIdentifier(context.getIdentifier(), context.getUserId());
        if (existing.isEmpty()) {
            return false;
        }
        // File content already stored — just create a new user_file record pointing at it
        FileDO file = existing.get(0);
        saveUserFileRecord(context.getUserId(), context.getFilename(),
                context.getParentId(), context.getFileType(), file.getId());

        publishUploadEvent(file, context.getUserId());
        return true;
    }

    // -------------------------------------------------------------------------
    // Single-file upload
    // -------------------------------------------------------------------------

    @Transactional(rollbackFor = Exception.class)
    public void upload(UploadFileContext context) {
        MultipartFile file = context.getFile();
        SaveFileContext saveCtx = new SaveFileContext();
        saveCtx.setFilename(file.getOriginalFilename());
        saveCtx.setTotalSize(file.getSize());
        saveCtx.setIdentifier(buildIdentifier(file));
        saveCtx.setUserId(context.getUserId());
        saveCtx.setFile(file);

        fileService.saveFile(saveCtx);

        UserFileDO userFile = saveUserFileRecord(context.getUserId(),
                file.getOriginalFilename(), context.getParentId(),
                context.getFileType(), saveCtx.getFileRecord().getId());

        publishUploadEvent(saveCtx.getFileRecord(), context.getUserId());
    }

    // -------------------------------------------------------------------------
    // Chunked upload — step 1: upload one chunk
    // -------------------------------------------------------------------------

    public Integer chunkUpload(FileChunkUploadContext context) {
        fileService.saveChunk(context);
        List<Integer> uploaded = fileService.getUploadedChunkNumbers(
                context.getIdentifier(), context.getUserId());
        boolean allDone = uploaded.size() >= context.getTotalChunks();
        // Return 1 = ready to merge, 0 = more chunks needed
        return allDone ? 1 : 0;
    }

    public List<Integer> getUploadedChunks(String identifier, Long userId) {
        return fileService.getUploadedChunkNumbers(identifier, userId);
    }

    // -------------------------------------------------------------------------
    // Chunked upload — step 2: merge
    // -------------------------------------------------------------------------

    @Transactional(rollbackFor = Exception.class)
    public void mergeFile(FileChunkMergeContext context) {
        FileDO file = fileService.mergeChunksAndSave(
                context.getFilename(), context.getIdentifier(),
                context.getTotalSize(), context.getUserId());

        saveUserFileRecord(context.getUserId(), context.getFilename(),
                context.getParentId(), context.getFileType(), file.getId());

        publishUploadEvent(file, context.getUserId());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private UserFileDO saveUserFileRecord(Long userId, String filename,
                                          Long parentId, Integer fileType, Long realFileId) {
        UserFileDO userFile = new UserFileDO();
        userFile.setId(IdUtil.get());
        userFile.setUserId(userId);
        userFile.setFilename(filename);
        userFile.setParentId(parentId);
        userFile.setFolderFlag(0);
        userFile.setFileType(fileType);
        userFile.setRealFileId(realFileId);
        userFile.setDelFlag(0);
        userFile.setCreateUser(userId);
        userFile.setUpdateUser(userId);
        userFile.setCreateTime(new Date());
        userFile.setUpdateTime(new Date());
        save(userFile);
        return userFile;
    }

    private void publishUploadEvent(FileDO file, Long userId) {
        FileUploadedEvent event = FileUploadedEvent.builder()
                .fileId(file.getId())
                .userId(userId)
                .filename(file.getFilename())
                .realPath(file.getRealPath())
                .fileSuffix(file.getFileSuffix())
                .fileSize(file.getFileSize())
                .build();
        streamBridge.send("fileUploaded-out-0", event);
    }

    private String buildIdentifier(MultipartFile file) {
        // In production: compute MD5 on the client and pass it as a request param.
        // Falling back to filename+size is weak but sufficient for the skeleton.
        return file.getOriginalFilename() + "_" + file.getSize();
    }

    // -------------------------------------------------------------------------
    // Download
    // -------------------------------------------------------------------------

    /**
     * Phase 1: validate ownership and populate ctx.filename + ctx.realPath.
     * No I/O — safe to call before setting HTTP response headers.
     */
    public void validateDownload(DownloadFileContext ctx) {
        UserFileDO userFile = getById(ctx.getUserFileId());
        if (userFile == null
                || userFile.getDelFlag() == 1
                || !userFile.getUserId().equals(ctx.getUserId())) {
            throw new BizException(404, "File not found");
        }
        if (userFile.getFolderFlag() == 1) {
            throw new BizException("Cannot download a folder");
        }
        FileDO file = fileService.getById(userFile.getRealFileId());
        if (file == null) {
            throw new BizException("Physical file record not found");
        }
        ctx.setFilename(userFile.getFilename());
        ctx.setRealPath(file.getRealPath());
    }

    /**
     * Phase 2: stream bytes from MinIO into ctx.outputStream.
     * Call only after validateDownload() has populated ctx.
     */
    public void executeDownload(DownloadFileContext ctx) {
        fileService.readFile(ctx.getRealPath(), ctx.getOutputStream());
    }

    // -------------------------------------------------------------------------
    // File listing
    // -------------------------------------------------------------------------

    public IPage<UserFileVO> listFiles(FileListContext ctx) {
        // Step 1: paginated query of the user's virtual file-system entries
        Page<UserFileDO> pageReq = new Page<>(ctx.getPage(), ctx.getSize());
        LambdaQueryWrapper<UserFileDO> q = new LambdaQueryWrapper<UserFileDO>()
                .eq(UserFileDO::getUserId, ctx.getUserId())
                .eq(UserFileDO::getParentId, ctx.getParentId())
                .eq(UserFileDO::getDelFlag, 0)
                .orderByDesc(UserFileDO::getFolderFlag)  // folders (1) before files (0)
                .orderByDesc(UserFileDO::getCreateTime);

        Page<UserFileDO> result = page(pageReq, q);

        // Step 2: batch-fetch physical file records to enrich with size/suffix
        List<Long> realFileIds = result.getRecords().stream()
                .filter(f -> f.getRealFileId() != null)
                .map(UserFileDO::getRealFileId)
                .collect(Collectors.toList());

        Map<Long, FileDO> fileMap = realFileIds.isEmpty()
                ? Collections.emptyMap()
                : fileService.listByIds(realFileIds).stream()
                        .collect(Collectors.toMap(FileDO::getId, f -> f));

        // Step 3: assemble VO list
        List<UserFileVO> voList = result.getRecords().stream()
                .map(uf -> toVO(uf, fileMap.get(uf.getRealFileId())))
                .collect(Collectors.toList());

        Page<UserFileVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    private UserFileVO toVO(UserFileDO uf, FileDO file) {
        UserFileVO vo = new UserFileVO();
        vo.setId(uf.getId());
        vo.setFilename(uf.getFilename());
        vo.setParentId(uf.getParentId());
        vo.setFolderFlag(uf.getFolderFlag());
        vo.setFileType(uf.getFileType());
        vo.setCreateTime(uf.getCreateTime());
        vo.setUpdateTime(uf.getUpdateTime());
        if (file != null) {
            vo.setFileSize(file.getFileSize());
            vo.setFileSuffix(file.getFileSuffix());
        }
        return vo;
    }

    // -------------------------------------------------------------------------
    // Soft delete (move to recycle bin)
    // -------------------------------------------------------------------------

    public void deleteFiles(UserFileDeleteContext ctx) {
        // Verify all IDs exist and belong to this user before touching anything
        List<UserFileDO> files = listByIds(ctx.getIds());
        if (files.size() != ctx.getIds().size()) {
            throw new BizException("Some files do not exist");
        }
        boolean allOwned = files.stream()
                .allMatch(f -> f.getUserId().equals(ctx.getUserId()));
        if (!allOwned) {
            throw new BizException(403, "Access denied");
        }

        // Batch update: set delFlag = 1 for all matched IDs
        LambdaUpdateWrapper<UserFileDO> update = new LambdaUpdateWrapper<UserFileDO>()
                .set(UserFileDO::getDelFlag, 1)
                .set(UserFileDO::getUpdateUser, ctx.getUserId())
                .set(UserFileDO::getUpdateTime, new Date())
                .in(UserFileDO::getId, ctx.getIds());
        update(update);
    }
}
