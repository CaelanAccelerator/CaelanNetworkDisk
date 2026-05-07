package com.disk.files.controller.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class DeleteFileRequest {

    @NotEmpty(message = "At least one file id is required")
    private List<Long> ids;
}
