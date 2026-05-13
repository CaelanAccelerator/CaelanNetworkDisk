package com.disk.files.controller.vo;

import lombok.Data;

import java.util.Date;

@Data
public class UserFileVO {

    private Long id;
    private String filename;
    private Long parentId;

    private Integer folderFlag;
    private Integer fileType;

    private Long fileSize;
    private String fileSuffix;
    private Date createTime;
    private Date updateTime;
}
