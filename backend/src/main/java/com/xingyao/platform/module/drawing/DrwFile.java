package com.xingyao.platform.module.drawing;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("drw_file")
public class DrwFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String fileName;
    private Long fileSize;
    private String status;
    private String recognizeResult;
    private String failReason;
    private Integer retryCount;
}
