package com.disk.files.domain.context;

import com.disk.files.domain.entity.FileDO;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SaveFileContext {
    private String filename;
    private Long totalSize;
    private String identifier;
    private Long userId;
    private MultipartFile file;

    private String realPath;

    private FileDO fileRecord;
}
