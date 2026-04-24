package com.cyan.datametric.application.metric.cmd;

import lombok.Data;

import java.util.List;

/**
 * SQL预览命令
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
public class SqlPreviewCmd {

    /**
     * 指标类型
     */
    private String metricType;

    /**
     * 定义体
     */
    private DefinitionBody definitionBody;

    @Data
    public static class DefinitionBody {
        private String statFunc;
        private String dsName;
        private String dbName;
        private String tblName;
        private String colName;
        private List<AtomicMetricCmd.FilterConditionCmd> filterCondition;
        private String atomicMetricId;
        private String timePeriodId;
        private List<String> modifierIds;
        private List<String> dimensionIds;
        private List<DerivedMetricCmd.GroupByFieldCmd> groupByFields;
        private String formula;
    }
}
