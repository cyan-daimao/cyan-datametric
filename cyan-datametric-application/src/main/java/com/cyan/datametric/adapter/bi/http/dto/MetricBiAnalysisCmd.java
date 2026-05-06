package com.cyan.datametric.adapter.bi.http.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 指标BI分析DSL请求体
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class MetricBiAnalysisCmd {

    /**
     * 图表类型：TABLE / BAR / LINE / PIE / SCATTER / AREA / NUMBER
     */
    @NotBlank(message = "图表类型不能为空")
    private String chartType;

    /**
     * 指标列表，至少1个
     */
    @NotEmpty(message = "指标列表不能为空")
    @Valid
    private List<MetricRef> metrics;

    /**
     * 维度列表，最多3个
     */
    @Valid
    private List<DimensionRef> dimensions;

    /**
     * 过滤条件
     */
    @Valid
    private List<FilterRef> filters;

    /**
     * 排序配置
     */
    @Valid
    private List<OrderRef> orders;

    /**
     * 限制条数，默认1000
     */
    private Integer limitValue;

    // ==================== 内部类 ====================

    @Data
    @Accessors(chain = true)
    public static class MetricRef {

        /**
         * 指标ID
         */
        @NotBlank(message = "指标ID不能为空")
        private String metricId;

        /**
         * 别名
         */
        private String alias;
    }

    @Data
    @Accessors(chain = true)
    public static class DimensionRef {

        /**
         * 维度ID
         */
        @NotBlank(message = "维度ID不能为空")
        private String dimId;

        /**
         * 别名
         */
        private String alias;
    }

    @Data
    @Accessors(chain = true)
    public static class FilterRef {

        /**
         * 指标ID（与dimId二选一）
         */
        private String metricId;

        /**
         * 维度ID（与metricId二选一）
         */
        private String dimId;

        /**
         * 操作符
         */
        @NotBlank(message = "操作符不能为空")
        private String operator;

        /**
         * 过滤值
         */
        private List<String> values;
    }

    @Data
    @Accessors(chain = true)
    public static class OrderRef {

        /**
         * 指标ID
         */
        private String metricId;

        /**
         * 维度ID
         */
        private String dimId;

        /**
         * 排序方向：ASC / DESC
         */
        @NotBlank(message = "排序方向不能为空")
        private String direction;
    }
}
