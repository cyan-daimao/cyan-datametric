package com.cyan.datametric.client.audience.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 指标人群圈选SQL DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricAudienceSelectionSqlDTO {

    /**
     * 查询特征哈希
     */
    private String queryHash;

    /**
     * 预估人数SQL
     */
    private String countSql;

    /**
     * 人群明细SQL
     */
    private String memberSql;

    /**
     * 实体ID列名
     */
    private String entityIdColumn;
}
