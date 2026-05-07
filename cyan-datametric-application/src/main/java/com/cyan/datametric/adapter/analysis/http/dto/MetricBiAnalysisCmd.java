package com.cyan.datametric.adapter.analysis.http.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 指标 BI 分析命令（前端 DSL 结构）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MetricBiAnalysisCmd {

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 指标列表
     */
    private List<MetricRef> metrics;

    /**
     * 维度列表
     */
    private List<DimensionRef> dimensions;

    /**
     * 过滤条件
     */
    private List<FilterRef> filters;

    /**
     * 排序配置
     */
    private List<OrderRef> orders;

    /**
     * 限制条数
     */
    private Integer limitValue;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class MetricRef {
        private String metricCode;
        private String alias;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class DimensionRef {
        private String dimCode;
        private String alias;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class FilterRef {
        private String metricCode;
        private String dimCode;
        private String operator;
        private List<String> values;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class OrderRef {
        private String metricCode;
        private String dimCode;
        private String direction;
    }
}
