package com.disk.files.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * User's virtual file-system entry.
 * Many UserFileDO rows can point to the same FileDO.realFileId (dedup).
 * Folders have realFileId = null and folderFlag = 1.
 */
@Data
@TableName("user_file")
public class UserFileDO {

    @TableId(type = IdType.INPUT)
    private Long id;
    private Long userId;
    private String filename;
    private Long parentId;
    /** 1 = folder, 0 = file */
    private Integer folderFlag;
    private Integer fileType;
    private Long realFileId;
    /** 0 = normal, 1 = deleted (soft delete → recycle bin) */
    private Integer delFlag;
    private Long createUser;
    private Long updateUser;
    private Date createTime;
    private Date updateTime;
}
