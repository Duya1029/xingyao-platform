package com.xingyao.platform.module.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("erp_order")
public class ErpOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private String type;
    private String partnerName;
    private BigDecimal amount;
    private Integer status;
    private LocalDate orderDate;
}
