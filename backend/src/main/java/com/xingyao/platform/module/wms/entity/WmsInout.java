package com.xingyao.platform.module.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wms_inout")
public class WmsInout {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private String type;
    private Long warehouseId;
    private Long targetWarehouseId;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String warehouseName;
}
