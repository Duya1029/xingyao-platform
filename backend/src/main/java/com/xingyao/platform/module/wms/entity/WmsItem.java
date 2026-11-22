package com.xingyao.platform.module.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wms_item")
public class WmsItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private Long categoryId;
    private Long brandId;
    private String spec;
    private String unit;
    private Integer status;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String categoryName;
    @TableField(exist = false)
    private String brandName;
    @TableField(exist = false)
    private java.math.BigDecimal stockQty;
}
