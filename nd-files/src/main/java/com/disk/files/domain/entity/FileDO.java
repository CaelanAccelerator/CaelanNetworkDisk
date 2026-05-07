package com.disk.files.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/** Physical file record — shared across users who own the same content (dedup via identifier). */
@Data
@TableName("file")
public class FileDO {

    @TableId(type = IdType.INPUT)
    private Long id;
    private String filename;
    private String realPath;
    private Long fileSize;
    private String fileSizeDesc;
    private String fileSuffix;
    private String filePreviewContentType;
    private String identifier;
    private Long createUser;
    private Date createTime;
    private Date updateTime;
}
