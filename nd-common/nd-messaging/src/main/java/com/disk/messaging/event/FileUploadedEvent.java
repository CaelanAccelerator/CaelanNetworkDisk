package com.disk.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadedEvent {
    private Long fileId;
    private Long userFileId;
    private Long userId;
    private String filename;
    private String realPath;
    private String fileSuffix;
    private Long fileSize;
}
