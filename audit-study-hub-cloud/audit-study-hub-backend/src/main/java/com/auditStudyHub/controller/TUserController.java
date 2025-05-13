package com.auditStudyHub.controller;

import com.auditStudyHub.common.Result;
import com.auditStudyHub.dto.*;
import com.auditStudyHub.mq.EmailMessageProducer;
import com.auditStudyHub.security.JwtTokenProvider;
import com.auditStudyHub.service.CaptchaService;
import com.auditStudyHub.service.TUserService;
import com.auditStudyHub.service.VerifyCodeService;
import com.auditStudyHub.utils.OSSUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.auditStudyHub.common.exception.ServiceException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;
import com.auditStudyHub.dto.ResetPasswordRequest;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class TUserController {

    private final TUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final VerifyCodeService verifyCodeService;
    private final EmailMessageProducer emailMessageProducer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CaptchaService captchaService;
    private final OSSUtils ossUtils;
    private static final Logger log = LoggerFactory.getLogger(TUserController.class);


    /**
     * 用户登录
     * 支持用户名、邮箱或学号登录
     *
     * @param loginRequest 登录请求DTO
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        // 调用service层处理登录逻辑
        LoginResponse response = userService.login(loginRequest.getAccount(),
                loginRequest.getPassword(),
                loginRequest.isRememberMe());

        return Result.success("登录成功", response);
    }

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("接收到注册请求: {}", registerRequest.getUsername());

        try {
            // 验证码校验
            boolean captchaValid = captchaService.verifyCaptcha(
                    registerRequest.getCaptchaId(),
                    registerRequest.getCaptchaCode()
            );

            if (!captchaValid) {
                log.warn("验证码验证失败: {}", registerRequest.getUsername());
                return Result.error("验证码错误或已过期");
            }

            // 调用service层处理注册逻辑
            LoginResponse response = userService.register(registerRequest);
            return Result.success("注册成功", response);
        } catch (ServiceException e) {
            log.warn("注册失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (DuplicateKeyException e) {
            String message = e.getMessage();
            log.warn("注册失败 - 数据重复: {}", message);

            if (message.contains("uk_phone")) {
                return Result.error("手机号已被注册");
            } else if (message.contains("uk_email")) {
                return Result.error("邮箱已被注册");
            } else if (message.contains("uk_username")) {
                return Result.error("用户名已存在");
            } else if (message.contains("uk_student_id")) {
                return Result.error("学号已被注册");
            }

            return Result.error("注册信息已存在");
        } catch (Exception e) {
            log.error("注册失败 - 系统异常: ", e);
            return Result.error("注册失败，请稍后再试");
        }
    }

    /**
     * 获取图形验证码
     *
     * @return 验证码响应
     */
    @GetMapping("/captcha")
    public Result<CaptchaResponse> getCaptcha() {
        try {
            CaptchaResponse captchaResponse = captchaService.generateCaptcha();
            return Result.success("验证码生成成功", captchaResponse);
        } catch (Exception e) {
            log.error("生成验证码失败: ", e);
            return Result.error("生成验证码失败，请稍后再试");
        }
    }

    /**
     * 验证图形验证码
     *
     * @param request 验证码验证请求
     * @return 验证结果
     */
    @PostMapping("/verify-captcha")
    public Result<Void> verifyCaptcha(@Valid @RequestBody VerifyCaptchaRequest request) {
        try {
            boolean valid = captchaService.verifyCaptcha(request.getCaptchaId(), request.getCaptchaCode());

            if (valid) {
                return Result.success("验证码验证成功");
            } else {
                return Result.error("验证码错误或已过期");
            }
        } catch (Exception e) {
            log.error("验证码验证失败: ", e);
            return Result.error("验证失败，请稍后再试");
        }
    }


    @PostMapping("validate-user")
    public Result<UserDTO> validateUser(@Valid @RequestBody ValidateUserRequest validateUserRequest) {
        log.info("接收到用户信息验证请求: {}", validateUserRequest.getUsername());

        try {
            // 调用service层处理验证逻辑
            UserDTO userDTO = userService.validateUserInfo(validateUserRequest);
            return Result.success("用户信息验证成功", userDTO);
        } catch (ServiceException e) {
            log.warn("用户信息验证失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("用户信息验证失败 - 系统异常: ", e);
            return Result.error("验证失败，请稍后再试");
        }
    }

    /**
     * 发送邮箱验证码
     *
     * @param request 包含用户名和邮箱的请求
     * @return 发送结果
     */
    @PostMapping("send-email-code")
    public Result<Void> sendEmailCode(@Valid @RequestBody SendEmailCodeRequest request) {
        log.info("接收到发送邮箱验证码请求: {} - {}", request.getUsername(), request.getEmail());

        try {
            // 调用服务层发送邮箱验证码
            userService.sendEmailVerificationCode(request.getUsername(), request.getEmail());
            return Result.success("验证码已发送，请查收邮件");
        } catch (ServiceException e) {
            log.warn("发送邮箱验证码失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("发送邮箱验证码失败 - 系统异常: ", e);
            return Result.error("发送验证码失败，请稍后再试");
        }
    }

    /**
     * 验证邮箱验证码
     *
     * @param request 包含邮箱和验证码的请求
     * @return 验证结果
     */
    @PostMapping("verify-email-code")
    public Result<Void> verifyEmailCode(@Valid @RequestBody VerifyEmailCodeRequest request) {
        log.info("接收到验证邮箱验证码请求: {}", request.getEmail());

        try {
            boolean isValid = verifyCodeService.verifyCode(request.getEmail(), request.getVerifyCode());

            if (isValid) {
                return Result.success("验证码验证成功");
            } else {
                return Result.error("验证码错误或已过期");
            }
        } catch (Exception e) {
            log.error("验证邮箱验证码失败 - 系统异常: ", e);
            return Result.error("验证失败，请稍后再试");
        }
    }

    /**
     * 发送手机验证码
     *
     * @param request 包含用户名和手机号的请求
     * @return 发送结果
     */
    @PostMapping("send-phone-code")
    public Result<Void> sendPhoneCode(@Valid @RequestBody SendPhoneCodeRequest request) {
        log.info("接收到发送手机验证码请求: {} - {}", request.getUsername(), request.getPhone());

        try {
            // 调用服务层发送手机验证码
            userService.sendPhoneVerificationCode(request.getUsername(), request.getPhone());
            return Result.success("验证码已发送，请注意查收短信");
        } catch (ServiceException e) {
            log.warn("发送手机验证码失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("发送手机验证码失败 - 系统异常: ", e);
            return Result.error("发送验证码失败，请稍后再试");
        }
    }

    /**
     * 验证手机验证码
     *
     * @param request 包含手机号和验证码的请求
     * @return 验证结果
     */
    @PostMapping("verify-phone-code")
    public Result<Void> verifyPhoneCode(@Valid @RequestBody VerifyPhoneCodeRequest request) {
        log.info("接收到验证手机验证码请求: {}", request.getPhone());

        try {
            boolean isValid = verifyCodeService.verifyCode(request.getPhone(), request.getVerifyCode());

            if (isValid) {
                return Result.success("验证码验证成功");
            } else {
                return Result.error("验证码错误或已过期");
            }
        } catch (Exception e) {
            log.error("验证手机验证码失败 - 系统异常: ", e);
            return Result.error("验证失败，请稍后再试");
        }
    }

    /**
     * 重置密码
     *
     * @param request 重置密码请求
     * @return 重置结果
     */
    @PostMapping("reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("接收到重置密码请求: {}", request.getUsername());

        try {
            // 调用服务层重置密码
            boolean success = userService.resetPassword(request);

            if (success) {
                return Result.success("密码重置成功，请使用新密码登录");
            } else {
                return Result.error("密码重置失败");
            }
        } catch (ServiceException e) {
            log.warn("密码重置失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("密码重置失败 - 系统异常: ", e);
            return Result.error("密码重置失败，请稍后再试");
        }
    }

    @PostMapping("password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        log.info("接收到更新密码请求: {}", request.getUsername());

        try {
            // 调用服务层更新密码
            boolean success = userService.updatePassword(request);

            if (success) {
                return Result.success("密码更新成功，请使用新密码登录");
            } else {
                return Result.error("密码更新失败");
            }
        } catch (ServiceException e) {
            log.warn("密码更新失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("密码更新失败 - 系统异常: ", e);
            return Result.error("密码更新失败，请稍后再试");
        }
    }

    /**
     * 更新用户信息
     *
     * @param request 更新用户信息请求
     * @return 更新结果
     */
    @PostMapping("updateUserInfo")
    public Result<UserDTO> updateUserInfo(@Valid @RequestBody UpdateUserInfoRequest request) {
        log.info("接收到更新用户信息请求: {}", request.getUsername());

        try {
            // 调用服务层更新用户信息
            UserDTO userDTO = userService.updateUserInfo(request);
            return Result.success("用户信息更新成功", userDTO);
        } catch (ServiceException e) {
            log.warn("更新用户信息失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新用户信息失败 - 系统异常: ", e);
            return Result.error("更新失败，请稍后再试");
        }
    }


    /**
     * 上传用户头像
     *
     * @param file 头像文件
     * @param username 用户名
     * @return 头像URL
     */
    @PostMapping("uploadAvatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                       @RequestParam("username") String username) {
        log.info("接收到上传头像请求: {}", username);

        try {
            // 验证文件是否为图片
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片格式的头像");
            }

            // 验证文件大小
            if (file.getSize() > 2 * 1024 * 1024) {
                return Result.error("头像大小不能超过2MB");
            }

            // 验证用户是否存在
            UserDTO userDTO = userService.findByUsername(username);
            if (userDTO == null) {
                return Result.error("用户不存在");
            }

            String avatarUrl = null;
            try {
                // 尝试使用阿里云OSS上传
                String folder = "avatars/" + username;
                avatarUrl = ossUtils.uploadFile(file, folder);
                log.info("头像成功上传到阿里云OSS: {}", avatarUrl);
            } catch (Exception e) {
                log.error("头像上传到阿里云OSS失败，尝试本地存储: {}", e.getMessage());
                // 如果阿里云OSS上传失败，保存到本地文件系统作为降级方案
                try {
                    // 确保目录存在
                    String uploadPath = "./upload/avatars/" + username;
                    java.io.File dir = new java.io.File(uploadPath);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    // 生成文件名
                    String fileName = UUID.randomUUID().toString().replace("-", "") + getFileExtension(file.getOriginalFilename());
                    String filePath = uploadPath + "/" + fileName;

                    // 保存文件
                    file.transferTo(new java.io.File(filePath));

                    // 生成访问URL (应用内相对路径)
                    avatarUrl = "/api/upload/avatars/" + username + "/" + fileName;
                    log.info("头像已保存到本地: {}", filePath);
                } catch (Exception localError) {
                    log.error("本地保存头像也失败: ", localError);
                    throw new ServiceException("头像上传失败，请联系管理员");
                }
            }

            if (avatarUrl != null) {
                log.info("头像上传成功: {}", avatarUrl);
                return Result.success("头像上传成功", avatarUrl);
            } else {
                return Result.error("头像上传失败");
            }
        } catch (ServiceException e) {
            log.warn("头像上传失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("头像上传失败 - 系统异常: ", e);
            return Result.error("头像上传失败，请稍后再试");
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 文件扩展名，包含点号
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return ".jpg"; // 默认扩展名
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }

    /**
     * 用户退出登录
     * 清除Redis中的token和用户信息
     *
     * @return 退出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        try {
            // 获取当前用户的认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                log.info("接收到退出登录请求: {}", username);

                // 调用服务层处理退出登录
                boolean success = userService.logout(username);

                if (success) {
                    // 清除Spring Security上下文中的认证信息
                    SecurityContextHolder.clearContext();
                    return Result.success("退出登录成功");
                } else {
                    return Result.error("退出登录失败");
                }
            }

            // 如果没有认证信息，直接清除上下文
            SecurityContextHolder.clearContext();
            return Result.success("退出登录成功");
        } catch (Exception e) {
            log.error("退出登录失败: ", e);
            return Result.error("退出登录失败，请稍后再试");
        }
    }

    /**
     * 发送新邮箱验证码
     *
     * @param request 包含用户名和新邮箱的请求
     * @return 发送结果
     */
    @PostMapping("send-new-email-code")
    public Result<Void> sendNewEmailCode(@Valid @RequestBody SendNewEmailCodeRequest request) {
        log.info("接收到发送新邮箱验证码请求: {} - {}", request.getUsername(), request.getNewEmail());

        try {
            // 调用服务层发送新邮箱验证码
            userService.sendNewEmailVerificationCode(request.getUsername(), request.getNewEmail());
            return Result.success("验证码已发送，请查收邮件");
        } catch (ServiceException e) {
            log.warn("发送新邮箱验证码失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("发送新邮箱验证码失败 - 系统异常: ", e);
            return Result.error("发送验证码失败，请稍后再试");
        }
    }

    /**
     * 更新用户邮箱
     *
     * @param request 更新邮箱请求
     * @return 更新结果
     */
    @PostMapping("update-email")
    public Result<UserDTO> updateEmail(@Valid @RequestBody UpdateEmailRequest request) {
        log.info("接收到更新邮箱请求: {} - {}", request.getUsername(), request.getNewEmail());

        try {
            // 调用服务层更新邮箱
            UserDTO userDTO = userService.updateUserEmail(request);
            return Result.success("邮箱更新成功", userDTO);
        } catch (ServiceException e) {
            log.warn("更新邮箱失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新邮箱失败 - 系统异常: ", e);
            return Result.error("更新邮箱失败，请稍后再试");
        }
    }
}