package com.cyan.datametric.adapter.metric.http.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.adapter.metric.http.dto.DashboardStatsDTO;
import com.cyan.datametric.adapter.metric.http.dto.SqlTrialResultDTO;
import com.cyan.datametric.adapter.metric.http.dto.SubjectDrilldownDTO;
import com.cyan.datametric.application.metric.bo.DashboardStatsBO;
import com.cyan.datametric.application.metric.bo.SqlTrialResultBO;
import com.cyan.datametric.application.metric.bo.SubjectDrilldownBO;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-24T02:23:14+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Arch Linux)"
)
public class MetricAdapterConvertImpl implements MetricAdapterConvert {

    private final MapstructConvert mapstructConvert = new MapstructConvert();

    @Override
    public SqlTrialResultDTO toSqlTrialResultDTO(SqlTrialResultBO bo) {
        if ( bo == null ) {
            return null;
        }

        SqlTrialResultDTO sqlTrialResultDTO = new SqlTrialResultDTO();

        sqlTrialResultDTO.setColumns( columnBOListToColumnDTOList( bo.getColumns() ) );
        List<List<Object>> list1 = bo.getRows();
        if ( list1 != null ) {
            sqlTrialResultDTO.setRows( new ArrayList<List<Object>>( list1 ) );
        }
        sqlTrialResultDTO.setSql( mapstructConvert.toString( bo.getSql() ) );
        sqlTrialResultDTO.setCostTime( mapstructConvert.toLong( bo.getCostTime() ) );

        return sqlTrialResultDTO;
    }

    @Override
    public DashboardStatsDTO toDashboardStatsDTO(DashboardStatsBO bo) {
        if ( bo == null ) {
            return null;
        }

        DashboardStatsDTO dashboardStatsDTO = new DashboardStatsDTO();

        dashboardStatsDTO.setTotalMetrics( mapstructConvert.toLong( bo.getTotalMetrics() ) );
        dashboardStatsDTO.setAtomicCount( mapstructConvert.toLong( bo.getAtomicCount() ) );
        dashboardStatsDTO.setDerivedCount( mapstructConvert.toLong( bo.getDerivedCount() ) );
        dashboardStatsDTO.setCompositeCount( mapstructConvert.toLong( bo.getCompositeCount() ) );
        dashboardStatsDTO.setPublishedCount( mapstructConvert.toLong( bo.getPublishedCount() ) );
        dashboardStatsDTO.setDraftCount( mapstructConvert.toLong( bo.getDraftCount() ) );
        dashboardStatsDTO.setOfflineCount( mapstructConvert.toLong( bo.getOfflineCount() ) );
        dashboardStatsDTO.setSubjectDistribution( subjectDistributionBOListToSubjectDistributionDTOList( bo.getSubjectDistribution() ) );
        dashboardStatsDTO.setRecentUpdates( recentUpdateBOListToRecentUpdateDTOList( bo.getRecentUpdates() ) );

        return dashboardStatsDTO;
    }

    @Override
    public SubjectDrilldownDTO toSubjectDrilldownDTO(SubjectDrilldownBO bo) {
        if ( bo == null ) {
            return null;
        }

        SubjectDrilldownDTO subjectDrilldownDTO = new SubjectDrilldownDTO();

        subjectDrilldownDTO.setSubjectCode( mapstructConvert.toString( bo.getSubjectCode() ) );
        subjectDrilldownDTO.setSubjectName( mapstructConvert.toString( bo.getSubjectName() ) );
        subjectDrilldownDTO.setTotalMetrics( mapstructConvert.toLong( bo.getTotalMetrics() ) );
        Map<String, Long> map = bo.getTypeDistribution();
        if ( map != null ) {
            subjectDrilldownDTO.setTypeDistribution( new LinkedHashMap<String, Long>( map ) );
        }
        Map<String, Long> map1 = bo.getStatusDistribution();
        if ( map1 != null ) {
            subjectDrilldownDTO.setStatusDistribution( new LinkedHashMap<String, Long>( map1 ) );
        }
        subjectDrilldownDTO.setChildren( subjectDrilldownBOListToSubjectDrilldownDTOList( bo.getChildren() ) );

        return subjectDrilldownDTO;
    }

    protected SqlTrialResultDTO.ColumnDTO columnBOToColumnDTO(SqlTrialResultBO.ColumnBO columnBO) {
        if ( columnBO == null ) {
            return null;
        }

        SqlTrialResultDTO.ColumnDTO columnDTO = new SqlTrialResultDTO.ColumnDTO();

        columnDTO.setName( mapstructConvert.toString( columnBO.getName() ) );
        columnDTO.setType( mapstructConvert.toString( columnBO.getType() ) );

        return columnDTO;
    }

    protected List<SqlTrialResultDTO.ColumnDTO> columnBOListToColumnDTOList(List<SqlTrialResultBO.ColumnBO> list) {
        if ( list == null ) {
            return null;
        }

        List<SqlTrialResultDTO.ColumnDTO> list1 = new ArrayList<SqlTrialResultDTO.ColumnDTO>( list.size() );
        for ( SqlTrialResultBO.ColumnBO columnBO : list ) {
            list1.add( columnBOToColumnDTO( columnBO ) );
        }

        return list1;
    }

    protected DashboardStatsDTO.SubjectDistributionDTO subjectDistributionBOToSubjectDistributionDTO(DashboardStatsBO.SubjectDistributionBO subjectDistributionBO) {
        if ( subjectDistributionBO == null ) {
            return null;
        }

        DashboardStatsDTO.SubjectDistributionDTO subjectDistributionDTO = new DashboardStatsDTO.SubjectDistributionDTO();

        subjectDistributionDTO.setSubjectCode( mapstructConvert.toString( subjectDistributionBO.getSubjectCode() ) );
        subjectDistributionDTO.setSubjectName( mapstructConvert.toString( subjectDistributionBO.getSubjectName() ) );
        subjectDistributionDTO.setCount( mapstructConvert.toLong( subjectDistributionBO.getCount() ) );

        return subjectDistributionDTO;
    }

    protected List<DashboardStatsDTO.SubjectDistributionDTO> subjectDistributionBOListToSubjectDistributionDTOList(List<DashboardStatsBO.SubjectDistributionBO> list) {
        if ( list == null ) {
            return null;
        }

        List<DashboardStatsDTO.SubjectDistributionDTO> list1 = new ArrayList<DashboardStatsDTO.SubjectDistributionDTO>( list.size() );
        for ( DashboardStatsBO.SubjectDistributionBO subjectDistributionBO : list ) {
            list1.add( subjectDistributionBOToSubjectDistributionDTO( subjectDistributionBO ) );
        }

        return list1;
    }

    protected DashboardStatsDTO.RecentUpdateDTO recentUpdateBOToRecentUpdateDTO(DashboardStatsBO.RecentUpdateBO recentUpdateBO) {
        if ( recentUpdateBO == null ) {
            return null;
        }

        DashboardStatsDTO.RecentUpdateDTO recentUpdateDTO = new DashboardStatsDTO.RecentUpdateDTO();

        recentUpdateDTO.setMetricCode( mapstructConvert.toString( recentUpdateBO.getMetricCode() ) );
        recentUpdateDTO.setMetricName( mapstructConvert.toString( recentUpdateBO.getMetricName() ) );
        recentUpdateDTO.setAction( mapstructConvert.toString( recentUpdateBO.getAction() ) );
        recentUpdateDTO.setOperator( mapstructConvert.toString( recentUpdateBO.getOperator() ) );
        recentUpdateDTO.setTime( recentUpdateBO.getTime() );

        return recentUpdateDTO;
    }

    protected List<DashboardStatsDTO.RecentUpdateDTO> recentUpdateBOListToRecentUpdateDTOList(List<DashboardStatsBO.RecentUpdateBO> list) {
        if ( list == null ) {
            return null;
        }

        List<DashboardStatsDTO.RecentUpdateDTO> list1 = new ArrayList<DashboardStatsDTO.RecentUpdateDTO>( list.size() );
        for ( DashboardStatsBO.RecentUpdateBO recentUpdateBO : list ) {
            list1.add( recentUpdateBOToRecentUpdateDTO( recentUpdateBO ) );
        }

        return list1;
    }

    protected List<SubjectDrilldownDTO> subjectDrilldownBOListToSubjectDrilldownDTOList(List<SubjectDrilldownBO> list) {
        if ( list == null ) {
            return null;
        }

        List<SubjectDrilldownDTO> list1 = new ArrayList<SubjectDrilldownDTO>( list.size() );
        for ( SubjectDrilldownBO subjectDrilldownBO : list ) {
            list1.add( toSubjectDrilldownDTO( subjectDrilldownBO ) );
        }

        return list1;
    }
}
