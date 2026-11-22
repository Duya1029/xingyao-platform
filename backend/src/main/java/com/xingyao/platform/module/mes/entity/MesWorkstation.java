package com.xingyao.platform.module.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("mes_workstation")
public class MesWorkstation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private Long workshopId;
    private Integer status;

    @TableField(exist = false)
    private String workshopName;
    @TableField(exist = false)
    private Integer memberCount;
}
