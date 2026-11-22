package com.xingyao.platform.module.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("mes_station_member")
public class MesStationMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long stationId;
    private Long userId;
}
