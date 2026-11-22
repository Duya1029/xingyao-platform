package com.xingyao.platform.module.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mes_task")
public class MesTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskNo;
    private Long workOrderId;
    private Long routeId;
    private Long workstationId;
    /** PERSON 指派到个人；STATION 指派到工作站（JB-1） */
    private String assignType;
    private Long assigneeId;
    private BigDecimal planQty;
    private BigDecimal finishedQty;
    private Integer status;
    private LocalDateTime planStart;
    private LocalDateTime planEnd;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String orderNo;
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String routeName;
    @TableField(exist = false)
    private String workstationName;
    @TableField(exist = false)
    private String assigneeName;
    /** 已报工（待审核 + 已通过）累计数量，用于 JB-4 提示剩余可报量 */
    @TableField(exist = false)
    private BigDecimal reportedQty;
    @TableField(exist = false)
    private BigDecimal remainQty;
}
