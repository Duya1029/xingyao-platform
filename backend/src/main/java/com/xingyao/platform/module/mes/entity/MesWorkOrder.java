package com.xingyao.platform.module.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mes_work_order")
public class MesWorkOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private String productName;
    private java.math.BigDecimal planQty;
    private Integer status;
    private LocalDateTime planStart;
    private LocalDateTime planEnd;
    private LocalDateTime createTime;
}
