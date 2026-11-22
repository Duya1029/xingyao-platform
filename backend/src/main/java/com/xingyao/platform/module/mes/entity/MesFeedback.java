package com.xingyao.platform.module.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mes_feedback")
public class MesFeedback {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String feedbackNo;
    private Long taskId;
    private Long userId;
    private BigDecimal qty;
    /** PENDING / APPROVED / REJECTED */
    private String status;
    private Long auditUserId;
    private LocalDateTime auditTime;
    private String auditRemark;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String taskNo;
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private BigDecimal planQty;
    @TableField(exist = false)
    private BigDecimal finishedQty;
}
