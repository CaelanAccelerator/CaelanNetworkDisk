package com.disk.files.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user_file")
public class UserFileDO {

    @TableId(type = IdType.INPUT)
    private Long id;
    private Long userId;
    private String filename;
    private Long parentId;

    private Integer folderFlag;
    private Integer fileType;
    private Long realFileId;

    private Integer delFlag;
    private Long createUser;
    private Long updateUser;
    private Date createTime;
    private Date updateTime;
}
