package com.disk.auth.domain.context;

import lombok.Data;

@Data
public class RegisterContext {
    private String username;
    private String password;
    private String email;
}
