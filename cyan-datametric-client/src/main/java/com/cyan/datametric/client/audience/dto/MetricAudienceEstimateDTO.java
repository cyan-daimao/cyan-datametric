package com.cyan.datametric.client.audience.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 指标人群预估 DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricAudienceEstimateDTO {

    /**
     * 查询特征哈希
     */
    private String queryHash;

    /**
     * 预估人数
     */
    private Long estimatedCount;

    /**
     * 预估SQL
     */
    private String countSql;
}
