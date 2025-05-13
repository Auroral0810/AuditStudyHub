package com.auditStudyHub.common.exception;

import com.auditStudyHub.common.Result;
import com.auditStudyHub.exception.EntityRelationException;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleServiceException(ServiceException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.error(e.getMessage()); // 只传递消息参数
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        StringBuilder errorMsg = new StringBuilder();
        
        for (FieldError fieldError : fieldErrors) {
            errorMsg.append(fieldError.getField())
                    .append(": ")
                    .append(fieldError.getDefaultMessage())
                    .append(", ");
        }
        
        String message = errorMsg.length() > 0 ? 
                errorMsg.substring(0, errorMsg.length() - 2) : "参数校验失败";
        
        log.error("参数校验异常：{}", message);
        return Result.error(message);
    }

    /**
     * 处理认证异常
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        log.error("认证异常：", e);
        String message = "认证失败";
        if (e instanceof BadCredentialsException) {
            message = "用户名或密码错误";
        }
        return Result.unauthorized(message);
    }

    /**
     * 处理授权异常
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.error("授权异常：", e);
        return Result.forbidden("没有权限执行此操作");
    }

    /**
     * 处理JWT过期异常
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(ExpiredJwtException.class)
    public Result<Void> handleExpiredJwtException(ExpiredJwtException e) {
        log.error("JWT过期异常：", e);
        return Result.unauthorized("登录已过期，请重新登录");
    }

    /**
     * 处理资源不存在异常
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NoHandlerFoundException.class, FileNotFoundException.class})
    public Result<Void> handleNotFoundException(Exception e) {
        log.error("资源不存在异常：", e);
        return Result.notFound("请求的资源不存在");
    }

    /**
     * 处理请求方法不支持异常
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("请求方法不支持异常：", e);
        return Result.error(405, "请求方法不支持");
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("文件上传大小超限异常：", e);
        return Result.error(413, "上传文件大小超出限制");
    }

    /**
     * 处理其他未预期的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error("系统异常，请联系管理员");
    }

    /**
     * 处理实体关系异常
     */
    @ExceptionHandler(EntityRelationException.class)
    public Result<?> handleEntityRelationException(EntityRelationException e) {
        log.error("实体关系异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }
} 