package com.disk.base.utils;

import cn.hutool.core.lang.Snowflake;

public class IdUtil {

    private static final Snowflake SNOWFLAKE = cn.hutool.core.util.IdUtil.getSnowflake(1, 1);

    public static long get() {
        return SNOWFLAKE.nextId();
    }
}
