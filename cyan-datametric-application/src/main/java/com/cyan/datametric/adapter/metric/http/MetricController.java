package com.cyan.datametric.adapter.metric.http;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.common.PageResultDTO;
import com.cyan.datametric.adapter.metric.http.convert.MetricAdapterConvert;
import com.cyan.datametric.adapter.metric.http.dto.*;
import com.cyan.datametric.application.metric.MetricService;
import com.cyan.datametric.application.metric.bo.*;
import com.cyan.datametric.application.metric.cmd.*;
import com.cyan.datametric.domain.metric.query.MetricPageQuery;
import com.cyan.employee.login.filter.UserContextHolder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 指标控制器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/metrics")
public class MetricController {

    private final MetricService metricService;

    public MetricController(MetricService metricService) {
        this.metricService = metricService;
    }

    // ==================== 测试数据初始化 ====================

    @PostMapping("/init-data")
    public Response<String> initData() {
        String user = "system";
        List<String> messages = new java.util.ArrayList<>();

        // 辅助方法：获取或创建原子指标
        java.util.function.Function<String, MetricBO> findByName = name -> {
            MetricPageQuery query = new MetricPageQuery();
            query.setMetricName(name);
            query.setPageSize(1);
            com.cyan.arch.common.api.Page<MetricBO> page = metricService.page(query, user);
            if (page.getData() != null && !page.getData().isEmpty()) {
                return page.getData().get(0);
            }
            return null;
        };

        // 1. 原子指标：订单总金额
        MetricBO atomic1 = findByName.apply("订单总金额");
        if (atomic1 == null) {
            AtomicMetricCmd cmd = new AtomicMetricCmd();
            cmd.setMetricName("订单总金额");
            cmd.setBizCaliber("统计所有订单的金额总和");
            cmd.setTechCaliber("SUM(order_amount) FROM cyan_ods.orders");
            cmd.setStatFunc("SUM");
            cmd.setDsName("mysql_prod");
            cmd.setDbName("cyan_ods");
            cmd.setTblName("orders");
            cmd.setColName("order_amount");
            cmd.setSubjectCode("ORDER");
            cmd.setCreateBy(user);
            cmd.setUpdateBy(user);
            atomic1 = metricService.createAtomic(cmd);
            messages.add("创建原子指标：订单总金额 (" + atomic1.getId() + ")");
        } else {
            messages.add("已存在原子指标：订单总金额 (" + atomic1.getId() + ")");
        }

        // 2. 原子指标：订单总笔数
        MetricBO atomic2 = findByName.apply("订单总笔数");
        if (atomic2 == null) {
            AtomicMetricCmd cmd = new AtomicMetricCmd();
            cmd.setMetricName("订单总笔数");
            cmd.setBizCaliber("统计所有订单的笔数");
            cmd.setTechCaliber("COUNT(order_id) FROM cyan_ods.orders");
            cmd.setStatFunc("COUNT");
            cmd.setDsName("mysql_prod");
            cmd.setDbName("cyan_ods");
            cmd.setTblName("orders");
            cmd.setColName("order_id");
            cmd.setSubjectCode("ORDER");
            cmd.setCreateBy(user);
            cmd.setUpdateBy(user);
            atomic2 = metricService.createAtomic(cmd);
            messages.add("创建原子指标：订单总笔数 (" + atomic2.getId() + ")");
        } else {
            messages.add("已存在原子指标：订单总笔数 (" + atomic2.getId() + ")");
        }

        // 3. 原子指标：活跃用户数
        MetricBO atomic3 = findByName.apply("活跃用户数");
        if (atomic3 == null) {
            AtomicMetricCmd cmd = new AtomicMetricCmd();
            cmd.setMetricName("活跃用户数");
            cmd.setBizCaliber("统计去重后的活跃用户数");
            cmd.setTechCaliber("COUNT_DISTINCT(user_id) FROM cyan_ods.user_login_log");
            cmd.setStatFunc("COUNT_DISTINCT");
            cmd.setDsName("mysql_prod");
            cmd.setDbName("cyan_ods");
            cmd.setTblName("user_login_log");
            cmd.setColName("user_id");
            cmd.setSubjectCode("USER");
            cmd.setCreateBy(user);
            cmd.setUpdateBy(user);
            atomic3 = metricService.createAtomic(cmd);
            messages.add("创建原子指标：活跃用户数 (" + atomic3.getId() + ")");
        } else {
            messages.add("已存在原子指标：活跃用户数 (" + atomic3.getId() + ")");
        }

        // 4. 派生指标：近7天订单金额（引用原子指标1）
        MetricBO derived1 = findByName.apply("近7天订单金额");
        if (derived1 == null) {
            DerivedMetricCmd cmd = new DerivedMetricCmd();
            cmd.setMetricName("近7天订单金额");
            cmd.setBizCaliber("统计近7天内的订单总金额");
            cmd.setTechCaliber("基于订单总金额原子指标，限定时间为近7天");
            cmd.setAtomicMetricId(atomic1.getId());
            cmd.setSubjectCode("ORDER");
            cmd.setCreateBy(user);
            cmd.setUpdateBy(user);
            derived1 = metricService.createDerived(cmd);
            messages.add("创建派生指标：近7天订单金额 (" + derived1.getId() + ")");
        } else {
            messages.add("已存在派生指标：近7天订单金额 (" + derived1.getId() + ")");
        }

        // 5. 复合指标：客单价（订单总金额 / 订单总笔数）
        MetricBO composite1 = findByName.apply("客单价");
        if (composite1 == null) {
            CompositeMetricCmd cmd = new CompositeMetricCmd();
            cmd.setMetricName("客单价");
            cmd.setBizCaliber("平均每笔订单的金额");
            cmd.setTechCaliber("订单总金额 / 订单总笔数");
            cmd.setFormula("${" + atomic1.getId() + "} / ${" + atomic2.getId() + "}");
            cmd.setMetricRefs(java.util.List.of(atomic1.getId(), atomic2.getId()));
            cmd.setSubjectCode("ORDER");
            cmd.setCreateBy(user);
            cmd.setUpdateBy(user);
            composite1 = metricService.createComposite(cmd);
            messages.add("创建复合指标：客单价 (" + composite1.getId() + ")");
        } else {
            messages.add("已存在复合指标：客单价 (" + composite1.getId() + ")");
        }

        return Response.success(String.join("\n", messages));
    }

    // ==================== 指标定义 ====================

    @GetMapping("/page")
    public Response<PageResultDTO<MetricDTO>> page(MetricPageQuery query) {
        String currentUser = UserContextHolder.getCurrentEmployee().getPassport();
        com.cyan.arch.common.api.Page<MetricBO> page = metricService.page(query, currentUser);
        return Response.success(new PageResultDTO<>(
                page.getData().stream().map(MetricAdapterConvert.INSTANCE::toMetricDTO).toList(),
                page.getCurrent(), page.getSize(), page.getTotal()));
    }

    @GetMapping("/{id}")
    public Response<MetricDetailDTO> detail(@PathVariable("id") String id) {
        MetricBO bo = metricService.detail(id);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricDetailDTO(bo));
    }

    @PostMapping("/atomic")
    public Response<MetricDTO> createAtomic(@RequestBody @Valid AtomicMetricCmd cmd) {
        cmd.setCreateBy(UserContextHolder.getCurrentEmployee().getPassport());
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        MetricBO bo = metricService.createAtomic(cmd);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricDTO(bo));
    }

    @PutMapping("/atomic/{id}")
    public Response<MetricDTO> updateAtomic(@PathVariable("id") String id, @RequestBody @Valid AtomicMetricCmd cmd) {
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        MetricBO bo = metricService.updateAtomic(id, cmd);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricDTO(bo));
    }

    @PostMapping("/derived")
    public Response<MetricDTO> createDerived(@RequestBody @Valid DerivedMetricCmd cmd) {
        cmd.setCreateBy(UserContextHolder.getCurrentEmployee().getPassport());
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        MetricBO bo = metricService.createDerived(cmd);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricDTO(bo));
    }

    @PutMapping("/derived/{id}")
    public Response<MetricDTO> updateDerived(@PathVariable("id") String id, @RequestBody @Valid DerivedMetricCmd cmd) {
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        MetricBO bo = metricService.updateDerived(id, cmd);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricDTO(bo));
    }

    @PostMapping("/composite")
    public Response<MetricDTO> createComposite(@RequestBody @Valid CompositeMetricCmd cmd) {
        cmd.setCreateBy(UserContextHolder.getCurrentEmployee().getPassport());
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        MetricBO bo = metricService.createComposite(cmd);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricDTO(bo));
    }

    @PutMapping("/composite/{id}")
    public Response<MetricDTO> updateComposite(@PathVariable("id") String id, @RequestBody @Valid CompositeMetricCmd cmd) {
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        MetricBO bo = metricService.updateComposite(id, cmd);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricDTO(bo));
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable("id") String id) {
        metricService.delete(id);
        return Response.success();
    }

    @PutMapping("/{id}/status")
    public Response<MetricDTO> updateStatus(@PathVariable("id") String id, @RequestBody UpdateStatusCmd cmd) {
        MetricBO bo = metricService.updateStatus(id, cmd);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricDTO(bo));
    }

    // ==================== SQL 预览与试算 ====================

    @PostMapping("/preview-sql")
    public Response<String> previewSql(@RequestBody SqlPreviewCmd cmd) {
        String sql = metricService.previewSql(cmd);
        return Response.success(sql);
    }

    @PostMapping("/trial")
    public Response<SqlTrialResultDTO> trialSql(@RequestBody SqlTrialCmd cmd) {
        SqlTrialResultBO bo = metricService.trialSql(cmd);
        return Response.success(MetricAdapterConvert.INSTANCE.toSqlTrialResultDTO(bo));
    }

    // ==================== 指标字典 ====================

    @GetMapping("/dictionary/page")
    public Response<PageResultDTO<DictionaryMetricDTO>> dictionaryPage(MetricPageQuery query) {
        String currentUser = UserContextHolder.getCurrentEmployee().getPassport();
        com.cyan.arch.common.api.Page<MetricBO> page = metricService.dictionaryPage(query, currentUser);
        return Response.success(new PageResultDTO<>(
                page.getData().stream().map(MetricAdapterConvert.INSTANCE::toDictionaryMetricDTO).toList(),
                page.getCurrent(), page.getSize(), page.getTotal()));
    }

    @PostMapping("/{id}/favorite")
    public Response<Void> favorite(@PathVariable("id") String id) {
        String userId = UserContextHolder.getCurrentEmployee().getPassport();
        metricService.favorite(id, userId);
        return Response.success();
    }

    @DeleteMapping("/{id}/favorite")
    public Response<Void> unfavorite(@PathVariable("id") String id) {
        String userId = UserContextHolder.getCurrentEmployee().getPassport();
        metricService.unfavorite(id, userId);
        return Response.success();
    }

    // ==================== 血缘 ====================

    @GetMapping("/{id}/lineage")
    public Response<LineageTreeDTO> lineage(
            @PathVariable("id") String id,
            @RequestParam(name = "direction", defaultValue = "BOTH") String direction,
            @RequestParam(name = "maxLevel", defaultValue = "3") int maxLevel) {
        LineageTreeBO bo = metricService.lineage(id, direction, maxLevel);
        return Response.success(MetricAdapterConvert.INSTANCE.toLineageTreeDTO(bo));
    }
}
