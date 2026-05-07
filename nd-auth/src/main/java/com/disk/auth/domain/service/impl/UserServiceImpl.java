package com.disk.auth.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.disk.auth.domain.context.LoginContext;
import com.disk.auth.domain.context.RegisterContext;
import com.disk.auth.domain.entity.UserDO;
import com.disk.auth.domain.service.UserService;
import com.disk.auth.infrastructure.mapper.UserMapper;
import com.disk.base.exception.BizException;
import com.disk.base.utils.IdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterContext ctx) {
        Long existing = userMapper.selectCount(
                new LambdaQueryWrapper<UserDO>()
                        .eq(UserDO::getUsername, ctx.getUsername())
        );
        if (existing > 0) {
            throw new BizException(409, "Username already taken");
        }

        UserDO user = new UserDO();
        user.setId(IdUtil.get());
        user.setUsername(ctx.getUsername());
        user.setPassword(passwordEncoder.encode(ctx.getPassword()));
        user.setEmail(ctx.getEmail());
        user.setTotalStorage(10L * 1024 * 1024 * 1024);
        user.setUsedStorage(0L);
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public Long login(LoginContext ctx) {
        UserDO user = userMapper.selectOne(
                new LambdaQueryWrapper<UserDO>()
                        .eq(UserDO::getUsername, ctx.getUsername())
        );
        if (user == null || !passwordEncoder.matches(ctx.getPassword(), user.getPassword())) {
            throw new BizException(401, "Invalid username or password");
        }
        if (user.getStatus() != 1) {
            throw new BizException(403, "Account is disabled");
        }
        return user.getId();
    }
}
