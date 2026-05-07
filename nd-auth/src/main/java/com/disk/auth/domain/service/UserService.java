package com.disk.auth.domain.service;

import com.disk.auth.domain.context.LoginContext;
import com.disk.auth.domain.context.RegisterContext;

public interface UserService {

    void register(RegisterContext ctx);

    Long login(LoginContext ctx);
}
