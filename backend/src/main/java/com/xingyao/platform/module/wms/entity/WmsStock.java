package com.xingyao.platform.module.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("wms_stock")
public class WmsStock {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long warehouseId;
    private Long itemId;
    private BigDecimal qty;

    @TableField(exist = false)
    private String warehouseName;
    @TableField(exist = false)
    private String itemName;
    @TableField(exist = false)
    private String itemCode;
    @TableField(exist = false)
    private String unit;
}
