package com.mimi.photowall.common;

import com.mimi.photowall.enums.ResultCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一接口返回体
 * <p>
 * 字段说明：
 * - success：是否成功
 * - code：状态码（200 成功，其他为失败）
 * - msg：提示信息
 * - data：响应数据
 * </p>
 *
 * @param <T> 响应数据类型
 */
@Data
@Schema(description = "统一返回结果")
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 是否成功 */
    @Schema(description = "是否成功", example = "true")
    private boolean success;

    /** 状态码 */
    @Schema(description = "状态码", example = "200")
    private int code;

    /** 提示信息 */
    @Schema(description = "提示信息", example = "操作成功")
    private String msg;

    /** 响应数据 */
    @Schema(description = "响应数据")
    private T data;

    private Result() {
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 成功（无数据）
     */
    public static <T> Result<T> ok() {
        Result<T> r = new Result<>();
        r.setSuccess(true);
        r.setCode(ResultCode.SUCCESS.getCode());
        r.setMsg(ResultCode.SUCCESS.getMsg());
        return r;
    }

    /**
     * 成功（带数据）
     */
    public static <T> Result<T> ok(T data) {
        Result<T> r = ok();
        r.setData(data);
        return r;
    }

    /**
     * 成功（自定义消息 + 数据）
     */
    public static <T> Result<T> ok(String msg, T data) {
        Result<T> r = ok(data);
        r.setMsg(msg);
        return r;
    }

    /**
     * 失败（使用枚举状态码）
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        Result<T> r = new Result<>();
        r.setSuccess(false);
        r.setCode(resultCode.getCode());
        r.setMsg(resultCode.getMsg());
        return r;
    }

    /**
     * 失败（自定义消息）
     */
    public static <T> Result<T> fail(String msg) {
        Result<T> r = new Result<>();
        r.setSuccess(false);
        r.setCode(ResultCode.INTERNAL_SERVER_ERROR.getCode());
        r.setMsg(msg);
        return r;
    }

    /**
     * 失败（枚举状态码 + 自定义消息）
     */
    public static <T> Result<T> fail(ResultCode resultCode, String msg) {
        Result<T> r = new Result<>();
        r.setSuccess(false);
        r.setCode(resultCode.getCode());
        r.setMsg(msg);
        return r;
    }

    /**
     * 失败（自定义状态码 + 消息）
     */
    public static <T> Result<T> fail(int code, String msg) {
        Result<T> r = new Result<>();
        r.setSuccess(false);
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
