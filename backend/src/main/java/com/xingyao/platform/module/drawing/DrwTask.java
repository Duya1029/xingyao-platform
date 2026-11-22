package com.xingyao.platform.module.drawing;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("drw_task")
public class DrwTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskNo;
    private Integer fileCount;
    private Integer successCount;
    private Integer failCount;
    private String status;
    private String createUser;
    private LocalDateTime createTime;
}
