package com.auditStudyHub.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回结果类
 */
@Data
@Schema(description = "通用返回结果")
public class Result<T> implements Serializable {

    @Schema(description = "状态码")
    private Integer code;
    
    @Schema(description = "返回消息")
    private String message;
    
    @Schema(description = "返回数据")
    private T data;
    
    // 成功状态码
    public static final Integer SUCCESS_CODE = 20000;
    // 失败状态码
    public static final Integer ERROR_CODE = 50000;
    // 未登录状态码
    public static final Integer UNAUTHORIZED_CODE = 40001;
    // 无权限状态码
    public static final Integer FORBIDDEN_CODE = 40003;
    // 资源不存在状态码
    public static final Integer NOT_FOUND_CODE = 40004;
    
    // 私有构造方法
    private Result() {}
    
    // 私有构造方法
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    // 返回成功结果
    public static <T> Result<T> success() {
        return success(null);
    }
    
    // 返回成功结果（带消息）
    public static <T> Result<T> success(String message) {
        return success(message, null);
    }
    
    // 返回成功结果（带数据）
    public static <T> Result<T> success(T data) {
        return success("操作成功", data);
    }
    
    // 返回成功结果（带消息和数据）
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
    
    // 返回失败结果
    public static <T> Result<T> error() {
        return error("操作失败");
    }
    
    // 返回失败结果（带消息）
    public static <T> Result<T> error(String message) {
        return error(message, null);
    }
    
    // 返回失败结果（带消息和状态码）
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
    
    // 返回未登录结果
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(UNAUTHORIZED_CODE, message, null);
    }
    
    // 返回无权限结果
    public static <T> Result<T> forbidden(String message) {
        return new Result<>(FORBIDDEN_CODE, message, null);
    }
    
    // 返回资源不存在结果
    public static <T> Result<T> notFound(String message) {
        return new Result<>(NOT_FOUND_CODE, message, null);
    }

    // 返回参数验证失败结果
    public static <T> Result<T> validateFailed(String message) {
        return new Result<>(40000, message, null);
    }

    // 返回失败结果（带消息和数据）
    public static <T> Result<T> error(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ERROR_CODE);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
} 