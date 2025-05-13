package com.auditStudyHub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String realName;
    private String studentId;
    private String email;
    private String phone;
    private String avatar;
    private Long collegeId;
    //学院名称
    private String collegeName;
    //专业ID
    private Long majorId;
    //专业名称
    private String majorName;
    private Integer role;
    private Integer status;
}


