package com.auditStudyHub.service;

import com.auditStudyHub.common.exception.ServiceException;
import com.auditStudyHub.dto.*;
import com.auditStudyHub.entity.TUser;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * 用户表(TUser)表服务接口
 *
 * @author Auroral
 * @since 2025-05-06 11:28:44
 */
public interface TUserService extends IService<TUser> {
    /**
     * 用户登录
     * 
     * @param account 账号（用户名、邮箱或学号）
     * @param password 密码
     * @param rememberMe 是否记住我
     * @return 登录响应信息
     */
    LoginResponse login(String account, String password, boolean rememberMe);

    /**
     * 注册用户
     *
     * @param registerRequest 注册请求
     * @return 登录响应（包含token和用户信息）
     */
    LoginResponse register(RegisterRequest registerRequest);
    
    /**
     * 验证用户信息
     * 用于找回密码第一步，验证用户名、学号和真实姓名是否匹配
     *
     * @param request 验证请求（包含用户名、学号和真实姓名）
     * @return 用户信息DTO
     */
    UserDTO validateUserInfo(ValidateUserRequest request);
    
    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户信息DTO，如果用户不存在则返回null
     */
    UserDTO findByUsername(String username);

    /**
     * 发送邮箱验证码
     *
     * @param username 用户名
     * @param email 邮箱
     * @return 发送结果，true表示成功，false表示失败
     * @throws ServiceException 如果用户不存在或邮箱不匹配或其他错误
     */
    boolean sendEmailVerificationCode(String username, String email);

    /**
     * 发送手机验证码
     *
     * @param username 用户名
     * @param phone 手机号
     * @return 发送结果，true表示成功，false表示失败
     * @throws ServiceException 如果用户不存在或手机号不匹配或其他错误
     */
    boolean sendPhoneVerificationCode(String username, String phone);

    /**
     * 重置密码
     *
     * @param resetPasswordRequest 重置密码请求
     * @return 是否重置成功
     * @throws ServiceException 如果验证失败或其他错误
     */
    boolean resetPassword(ResetPasswordRequest resetPasswordRequest);

    /**
     * 更新用户信息
     *
     * @param updateUserInfoRequest 更新用户信息请求
     * @return 更新后的用户信息
     * @throws ServiceException 如果更新失败或其他错误
     */
    UserDTO updateUserInfo(UpdateUserInfoRequest updateUserInfoRequest);

    /**
     * 更新用户密码
     *
     * @param updatePasswordRequest 更新密码请求
     * @return 是否更新成功
     * @throws ServiceException 如果验证失败或其他错误
     */
    boolean updatePassword(UpdatePasswordRequest updatePasswordRequest);
    
    /**
     * 用户退出登录
     * 清除Redis中的token和用户信息缓存
     *
     * @param username 用户名
     * @return 是否退出成功
     */
    boolean logout(String username);

    /**
     * 发送新邮箱验证码
     *
     * @param username 用户名
     * @param newEmail 新邮箱
     * @return 发送结果，true表示成功，false表示失败
     * @throws ServiceException 如果用户不存在或新邮箱不匹配或其他错误
     */
    boolean sendNewEmailVerificationCode(String username, String newEmail);

    /**
     * 更新用户邮箱
     *
     * @param request 更新邮箱请求
     * @return 更新后的用户信息
     * @throws ServiceException 如果更新失败或其他错误
     */
    UserDTO updateUserEmail(UpdateEmailRequest request);
}

