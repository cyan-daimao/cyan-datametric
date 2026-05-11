package com.cyan.datametric.client;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.client.dto.MetricBiAnalysisCmd;
import com.cyan.datametric.client.dto.MetricBiChartDataDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 指标 BI 分析 RPC 客户端（服务间内部调用，无登录拦截器）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@FeignClient(name = "cyan-datametric", contextId = "cyan-datametric.analysis", path = "/rpc/v1/metrics/bi/analysis")
public interface MetricBiAnalysisClient {

    /**
     * 执行指标分析
     *
     * @param cmd 指标分析命令
     * @return 图表数据
     */
    @PostMapping("/execute")
    Response<MetricBiChartDataDTO> execute(@RequestBody MetricBiAnalysisCmd cmd);

    /**
     * 预览指标分析 SQL
     *
     * @param cmd 指标分析命令
     * @return 生成的 SQL
     */
    @PostMapping("/preview-sql")
    Response<String> previewSql(@RequestBody MetricBiAnalysisCmd cmd);
}
