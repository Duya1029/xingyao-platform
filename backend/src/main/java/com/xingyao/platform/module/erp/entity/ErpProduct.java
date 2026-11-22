package com.xingyao.platform.module.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("erp_product")
public class ErpProduct {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private String unit;
    private String categoryName;
    private Integer status;
}
