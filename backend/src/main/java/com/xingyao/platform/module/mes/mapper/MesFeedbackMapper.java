package com.xingyao.platform.module.mes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingyao.platform.module.mes.entity.MesFeedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface MesFeedbackMapper extends BaseMapper<MesFeedback> {

    @Select("SELECT ISNULL(SUM(qty),0) FROM mes_feedback WHERE task_id = #{taskId} AND status = 'APPROVED' AND id <> ISNULL(#{excludeId}, 0)")
    BigDecimal sumApprovedQty(@Param("taskId") Long taskId, @Param("excludeId") Long excludeId);
}
