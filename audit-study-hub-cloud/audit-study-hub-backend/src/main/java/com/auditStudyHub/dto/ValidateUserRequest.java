package com.auditStudyHub.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class ValidateUserRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "学号不能为空")
    private String studentId;

    @NotBlank(message = "姓名不能为空")
    private String realName;
}