package com.disk.files.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("file_chunk")
public class FileChunkDO {

    @TableId(type = IdType.INPUT)
    private Long id;
    private String identifier;
    private String realPath;
    private Integer chunkNumber;
    private Long currentChunkSize;
    private Long totalSize;
    private Long createUser;
    private Date createTime;
    private Date updateTime;

    private Date expirationTime;
}
