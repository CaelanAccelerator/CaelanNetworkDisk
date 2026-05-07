package com.disk.files.domain.context;

import lombok.Data;

import java.util.List;

@Data
public class UserFileDeleteContext {
    private Long userId;
    private List<Long> ids;
}
