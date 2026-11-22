package com.xingyao.platform.module.importer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_import_error")
public class SysImportError {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long batchId;
    private Integer rowNo;
    private String columnName;
    private String message;
    private String rawData;
}
