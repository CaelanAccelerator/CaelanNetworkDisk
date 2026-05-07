package com.disk.auth.domain.context;

import lombok.Data;

@Data
public class LoginContext {
    private String username;
    private String password;
}
