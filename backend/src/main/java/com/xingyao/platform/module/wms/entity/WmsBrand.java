package com.xingyao.platform.module.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("wms_brand")
public class WmsBrand {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer status;
}
