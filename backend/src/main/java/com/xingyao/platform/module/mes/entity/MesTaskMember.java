package com.xingyao.platform.module.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("mes_task_member")
public class MesTaskMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long userId;
    /** ASSIGNEE 指派个人 / STATION 工作站成员 / TEAM 工单团队 */
    private String source;
}
