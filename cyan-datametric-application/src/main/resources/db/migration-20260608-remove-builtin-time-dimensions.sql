-- 删除系统默认时间维度兜底后的数据清理脚本
-- 仅清理未配置字段绑定的默认时间维度编码，避免误删用户已经维护过绑定的维度。

UPDATE metric_dimension
SET deleted_at = NOW()
WHERE dim_code IN (
    'DIM_DATE_YEAR',
    'DIM_DATE_QUARTER',
    'DIM_DATE_MONTH',
    'DIM_DATE_WEEK',
    'DIM_DATE_DAY',
    'DIM_DATE_HOUR',
    'DIM_DATE_MINUTE',
    'DIM_DATE_SECOND',
    'DIM_TIME_HOUR',
    'DIM_TIME_MINUTE',
    'DIM_TIME_SECOND'
)
AND deleted_at IS NULL
AND NOT EXISTS (
    SELECT 1
    FROM metric_dimension_binding
    WHERE metric_dimension_binding.dim_id = metric_dimension.id
      AND metric_dimension_binding.deleted_at IS NULL
);
