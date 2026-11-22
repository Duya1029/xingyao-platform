package com.xingyao.platform.common;

/**
 * 业务异常：由全局异常处理器转换为统一返回结构
 */
public class BizException extends RuntimeException {
    private final int code;

    public BizException(String message) {
        this(400, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
