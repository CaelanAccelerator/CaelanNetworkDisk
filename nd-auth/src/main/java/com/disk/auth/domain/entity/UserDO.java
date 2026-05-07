package com.disk.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("nd_user")
public class UserDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String username;

    private String password;

    private String email;

    private Long totalStorage;

    private Long usedStorage;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
