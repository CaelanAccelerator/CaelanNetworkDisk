package com.disk.files.domain.context;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadFileContext {
    private Long userId;
    private Long parentId;
    private Integer fileType;
    private MultipartFile file;
}
