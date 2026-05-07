package com.cyan.datametric.adapter.semantic.http;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.semantic.http.dto.SemanticQueryCmdDTO;
import com.cyan.datametric.adapter.semantic.http.dto.SemanticQueryResultDTO;
import com.cyan.datametric.application.semantic.SemanticQueryEngine;
import com.cyan.datametric.application.semantic.SemanticSqlBuilder;
import com.cyan.employee.login.filter.UserContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * 语义层控制器
 * <p>
 * 提供语义查询、SQL 预览等 REST API。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/semantic")
@RequiredArgsConstructor
public class SemanticController {

    private final SemanticQueryEngine semanticQueryEngine;

    /**
     * 语义查询执行
     */
    @PostMapping("/query")
    public Response<SemanticQueryResultDTO> query(@RequestBody @Valid SemanticQueryCmdDTO dto) {
        SemanticQueryEngine.SemanticQueryCmd cmd = toCmd(dto);
        String executor = UserContextHolder.getCurrentEmployee().getPassport();
        SemanticQueryEngine.SemanticQueryResult result = semanticQueryEngine.execute(cmd, executor);
        return Response.success(toDTO(result));
    }

    /**
     * SQL 预览（不执行）
     */
    @PostMapping("/preview-sql")
    public Response<String> previewSql(@RequestBody @Valid SemanticQueryCmdDTO dto) {
        SemanticQueryEngine.SemanticQueryCmd cmd = toCmd(dto);
        String sql = semanticQueryEngine.previewSql(cmd);
        return Response.success(sql);
    }

    private SemanticQueryEngine.SemanticQueryCmd toCmd(SemanticQueryCmdDTO dto) {
        SemanticQueryEngine.SemanticQueryCmd cmd = new SemanticQueryEngine.SemanticQueryCmd();
        cmd.setMetricCodes(dto.getMetricCodes());
        if (dto.getDimensions() != null) {
            cmd.setDimensions(dto.getDimensions().stream()
                    .map(d -> new SemanticSqlBuilder.DimensionRef()
                            .setTableId(d.getTableId())
                            .setColumnName(d.getColumnName())
                            .setAlias(d.getAlias()))
                    .collect(Collectors.toList()));
        }
        if (dto.getFilters() != null) {
            cmd.setFilters(dto.getFilters().stream()
                    .map(f -> new SemanticSqlBuilder.FilterRef()
                            .setTableId(f.getTableId())
                            .setColumnName(f.getColumnName())
                            .setOperator(f.getOperator())
                            .setValues(f.getValues()))
                    .collect(Collectors.toList()));
        }
        if (dto.getOrders() != null) {
            cmd.setOrders(dto.getOrders().stream()
                    .map(o -> new SemanticSqlBuilder.OrderRef()
                            .setMetricCode(o.getMetricCode())
                            .setTableId(o.getTableId())
                            .setColumnName(o.getColumnName())
                            .setDirection(o.getDirection()))
                    .collect(Collectors.toList()));
        }
        cmd.setLimit(dto.getLimit());
        return cmd;
    }

    private SemanticQueryResultDTO toDTO(SemanticQueryEngine.SemanticQueryResult result) {
        SemanticQueryResultDTO dto = new SemanticQueryResultDTO();
        dto.setStatus(result.getStatus());
        dto.setSql(result.getSql());
        dto.setRouteType(result.getRouteType());
        dto.setCostTimeMs(result.getCostTimeMs());
        dto.setErrorMessage(result.getErrorMessage());
        dto.setColumns(result.getColumns());
        dto.setRows(result.getRows());
        return dto;
    }
}
