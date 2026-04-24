package com.cyan.datametric.application.metric.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * SQL试算结果BO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class SqlTrialResultBO {

    /**
     * 列信息
     */
    private List<ColumnBO> columns;

    /**
     * 行数据
     */
    private List<List<Object>> rows;

    /**
     * SQL
     */
    private String sql;

    /**
     * 耗时(ms)
     */
    private Long costTime;

    @Data
    @Accessors(chain = true)
    public static class ColumnBO {
        private String name;
        private String type;
    }
}
