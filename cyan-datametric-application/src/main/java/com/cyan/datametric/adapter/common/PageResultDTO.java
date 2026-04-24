package com.cyan.datametric.adapter.common;

import lombok.Data;

import java.util.List;

/**
 * 分页结果DTO（与前端/契约字段名对齐）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
public class PageResultDTO<T> {

    private List<T> list;
    private long total;
    private long pageNum;
    private long pageSize;

    public PageResultDTO() {
    }

    public PageResultDTO(List<T> list, long pageNum, long pageSize, long total) {
        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
    }
}
