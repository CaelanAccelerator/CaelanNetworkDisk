package com.disk.files.controller.vo;

import lombok.Data;

import java.util.Date;

@Data
public class UserFileVO {

    private Long id;
    private String filename;
    private Long parentId;
    /** 1 = folder, 0 = file */
    private Integer folderFlag;
    private Integer fileType;
    /** Null for folders; populated from FileDO for files */
    private Long fileSize;
    private String fileSuffix;
    private Date createTime;
    private Date updateTime;
}
