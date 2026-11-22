package com.xingyao.platform.module.importer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_import_batch")
public class SysImportBatch {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String batchNo;
    private String bizType;
    private String fileName;
    private Integer totalRows;
    private Integer successRows;
    private Integer failRows;
    private String status;
    private String createUser;
    private LocalDateTime createTime;
}
