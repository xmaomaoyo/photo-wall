package com.mimi.photowall.exception;

import com.mimi.photowall.enums.ResultCode;
import lombok.Getter;

/**
 * 业务异常类
 * 用于处理业务逻辑中的异常情况
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 状态码
     */
    private final int code;

    /**
     * 提示信息
     */
    private final String msg;

    /**
     * 构造方法
     *
     * @param resultCode 状态码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
    }

    /**
     * 构造方法
     *
     * @param resultCode 状态码枚举
     * @param msg        自定义提示信息
     */
    public BusinessException(ResultCode resultCode, String msg) {
        super(msg);
        this.code = resultCode.getCode();
        this.msg = msg;
    }

    /**
     * 构造方法
     *
     * @param code 状态码
     * @param msg  提示信息
     */
    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
