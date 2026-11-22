package com.xingyao.platform.module.mes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingyao.platform.module.mes.entity.MesTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface MesTaskMapper extends BaseMapper<MesTask> {

    @Select("SELECT ISNULL(SUM(qty),0) FROM mes_feedback WHERE task_id = #{taskId} AND status IN ('PENDING','APPROVED')")
    BigDecimal sumReportedQty(@Param("taskId") Long taskId);
}
