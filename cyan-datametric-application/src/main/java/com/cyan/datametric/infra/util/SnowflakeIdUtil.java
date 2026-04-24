package com.cyan.datametric.infra.util;

import com.cyan.arch.common.util.Snowflake;

/**
 * 雪花ID工具
 *
 * @author cy.Y
 * @since 1.0.0
 */
public class SnowflakeIdUtil {

    private SnowflakeIdUtil() {
    }

    /**
     * 生成下一个ID
     */
    public static long nextId() {
        return Snowflake.nextId();
    }

    /**
     * 生成下一个ID字符串
     */
    public static String nextIdStr() {
        return String.valueOf(Snowflake.nextId());
    }
}
