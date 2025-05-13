package com.auditStudyHub.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 重置密码请求DTO
 */
@Data
public class ResetPasswordRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "学号不能为空")
    private String studentId;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "联系方式类型不能为空")
    @Pattern(regexp = "email|phone", message = "联系方式类型必须是email或phone")
    private String contactType;

    @NotBlank(message = "联系方式不能为空")
    private String contact;  // 邮箱或手机号

    @NotBlank(message = "验证码不能为空")
    private String verifyCode;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, message = "密码长度不能小于6位")
    private String newPassword;
} 