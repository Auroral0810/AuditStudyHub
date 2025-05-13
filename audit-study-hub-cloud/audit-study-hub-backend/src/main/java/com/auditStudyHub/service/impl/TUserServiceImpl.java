package com.auditStudyHub.service.impl;

import com.auditStudyHub.common.exception.ServiceException;
import com.auditStudyHub.dto.*;
import com.auditStudyHub.entity.TUser;
import com.auditStudyHub.mapper.TUserMapper;
import com.auditStudyHub.security.JwtTokenProvider;
import com.auditStudyHub.service.TUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auditStudyHub.mq.EmailMessageProducer;
import com.auditStudyHub.service.VerifyCodeService;

/**
 * 用户表(TUser)表服务实现类
 *
 * @author Auroral
 * @since 2025-05-06 11:28:44
 */
@Service("tUserService")
public class TUserServiceImpl extends ServiceImpl<TUserMapper, TUser> implements TUserService {

    private static final Logger log = LoggerFactory.getLogger(TUserServiceImpl.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private VerifyCodeService verifyCodeService;
    @Autowired
    private EmailMessageProducer emailMessageProducer;

    @Override
    public LoginResponse login(String account, String password, boolean rememberMe) {
        log.debug("开始登录处理: {}", account);

        // 1. 先尝试检查Redis缓存
        String accountKey = null;
        String username = null;

        // 确定缓存键格式
        if (account.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            accountKey = "account:email:" + account;
        } else if (account.matches("^\\d+$")) {
            accountKey = "account:studentId:" + account;
        } else {
            accountKey = "account:username:" + account;
        }

        // 登录限流检查 - 必须在任何数据库操作之前执行
        checkLoginAttemptLimit(account);

        // 尝试从Redis获取用户名
        username = (String) redisTemplate.opsForValue().get(accountKey);
        log.debug("从Redis查找账户映射: {} -> {}", accountKey, username);

        // 检查Redis是否有用户信息缓存
        TUser user = null;
        if (username != null) {
            String userKey = "user:" + username;
            UserDTO cachedUserDTO = (UserDTO) redisTemplate.opsForValue().get(userKey);
            log.debug("从Redis获取用户信息: {} -> {}", userKey, cachedUserDTO != null ? "找到缓存" : "未找到缓存");

            if (cachedUserDTO != null) {
                // 从数据库获取用户以验证密码 (因为缓存中没有密码)
                user = this.getOne(new LambdaQueryWrapper<TUser>()
                        .eq(TUser::getUsername, username)
                        .eq(TUser::getIsDeleted, 0));
                log.debug("需要从数据库获取用户密码信息: {}", user != null ? "成功" : "失败");
            }
        }

        // 如果Redis没有，则从数据库查询
        if (user == null) {
            log.debug("从数据库查询用户: {}", account);
            // 原有的数据库查询逻辑
            if (account.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                user = this.getOne(new LambdaQueryWrapper<TUser>()
                        .eq(TUser::getEmail, account)
                        .eq(TUser::getIsDeleted, 0));
            } else if (account.matches("^\\d+$")) {
                user = this.getOne(new LambdaQueryWrapper<TUser>()
                        .eq(TUser::getStudentId, account)
                        .eq(TUser::getIsDeleted, 0));
            } else {
                user = this.getOne(new LambdaQueryWrapper<TUser>()
                        .eq(TUser::getUsername, account)
                        .eq(TUser::getIsDeleted, 0));
            }

            if (user == null) {
                // 记录登录失败
                incrementLoginAttempt(account);
                throw new ServiceException("用户不存在");
            }

            // 数据库查询成功后，更新缓存
            if (user != null) {
                // 这里添加缓存逻辑
                redisTemplate.opsForValue().set(accountKey, user.getUsername(), 24, TimeUnit.HOURS);
                log.debug("更新Redis账户映射缓存: {} -> {}", accountKey, user.getUsername());
            }
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            // 记录登录失败
            incrementLoginAttempt(account);
            throw new ServiceException("密码错误");
        }

        // 3. 检查用户状态
        if (user.getStatus() != 1) {
            // 记录登录失败
            incrementLoginAttempt(account);
            throw new ServiceException("账号已被禁用");
        }

        // 登录成功，重置登录失败计数
        resetLoginAttempt(account);

        // 4-11. 生成认证信息、JWT令牌并缓存（保持原有逻辑）
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole() == 1 ? "ROLE_ADMIN" : "ROLE_USER"));

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        String jwt = jwtTokenProvider.createToken(authenticationToken, rememberMe);

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        long expiration = rememberMe ?
                7 * 24 * 60 * 60 :
                24 * 60 * 60;

        redisTemplate.opsForValue().set(
                "token:" + user.getUsername(),
                jwt,
                expiration,
                TimeUnit.SECONDS
        );

        redisTemplate.opsForValue().set(
                "user:" + user.getUsername(),
                userDTO,
                expiration,
                TimeUnit.SECONDS
        );

        return LoginResponse.builder()
                .token(jwt)
                .user(userDTO)
                .build();
    }

    /**
     * 检查登录尝试次数限制
     * @param account 账号
     */
    private void checkLoginAttemptLimit(String account) {
        String loginAttemptsKey = "login_attempts:" + account;
        String lockKey = "login_lock:" + account;

        // 检查是否被锁定
        Boolean isLocked = redisTemplate.hasKey(lockKey);
        if (Boolean.TRUE.equals(isLocked)) {
            // 获取剩余锁定时间
            Long remainingSeconds = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
            throw new ServiceException("账号已被锁定，请" + remainingSeconds + "秒后重试");
        }
    }

    /**
     * 增加登录尝试失败次数
     * @param account 账号
     */
    private void incrementLoginAttempt(String account) {
        String loginAttemptsKey = "login_attempts:" + account;
        String lockKey = "login_lock:" + account;

        // 增加失败计数
        Long attempts = redisTemplate.opsForValue().increment(loginAttemptsKey);
        // 设置24小时后过期，防止长期占用内存
        redisTemplate.expire(loginAttemptsKey, 24, TimeUnit.HOURS);

        // 如果失败5次，锁定1分钟
        if (attempts != null && attempts >= 5) {
            redisTemplate.opsForValue().set(lockKey, "locked", 1, TimeUnit.MINUTES);
            // 重置尝试次数
            redisTemplate.delete(loginAttemptsKey);
            throw new ServiceException("登录失败次数过多，账号已被锁定1分钟");
        }
    }

    /**
     * 重置登录尝试次数
     * @param account 账号
     */
    private void resetLoginAttempt(String account) {
        String loginAttemptsKey = "login_attempts:" + account;
        redisTemplate.delete(loginAttemptsKey);
    }

    @Override
    public LoginResponse register(RegisterRequest registerRequest) {
        // 1. 检查唯一性约束
        // 检查用户名是否已存在
        if (this.count(new LambdaQueryWrapper<TUser>()
                .eq(TUser::getUsername, registerRequest.getUsername())
                .eq(TUser::getIsDeleted, 0)) > 0) {
            throw new ServiceException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (StringUtils.hasText(registerRequest.getEmail()) && this.count(new LambdaQueryWrapper<TUser>()
                .eq(TUser::getEmail, registerRequest.getEmail())
                .eq(TUser::getIsDeleted, 0)) > 0) {
            throw new ServiceException("邮箱已被注册");
        }

        // 检查学号是否已存在
        if (StringUtils.hasText(registerRequest.getStudentId()) && this.count(new LambdaQueryWrapper<TUser>()
                .eq(TUser::getStudentId, registerRequest.getStudentId())
                .eq(TUser::getIsDeleted, 0)) > 0) {
            throw new ServiceException("学号已被注册");
        }

        // 检查手机号是否已存在
        if (StringUtils.hasText(registerRequest.getPhone()) && this.count(new LambdaQueryWrapper<TUser>()
                .eq(TUser::getPhone, registerRequest.getPhone())
                .eq(TUser::getIsDeleted, 0)) > 0) {
            throw new ServiceException("手机号已被注册");
        }

        // 2. 创建新用户
        TUser user = new TUser();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRealName(registerRequest.getRealName());
        user.setStudentId(registerRequest.getStudentId());
        user.setPhone(registerRequest.getPhone());
        user.setCollegeId(registerRequest.getCollegeId());
        user.setMajorId(registerRequest.getMajorId());
        user.setRole(0); // 默认为普通用户
        user.setStatus(1); // 默认启用状态
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDeleted(0);

        // 3. 保存用户
        this.save(user);

        // 4. 生成认证信息
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // 注册用户默认为普通用户角色

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);

        // 5. 设置认证信息到上下文
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 6. 生成JWT令牌
        String jwt = jwtTokenProvider.createToken(authenticationToken, false);

        // 7. 转换为DTO
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        // 8. 将用户信息和令牌保存到Redis
        // 设置默认过期时间为24小时
        long expiration = 24 * 60 * 60;

        // 保存token到Redis
        redisTemplate.opsForValue().set(
                "token:" + user.getUsername(),
                jwt,
                expiration,
                TimeUnit.SECONDS
        );

        // 保存用户信息到Redis
        redisTemplate.opsForValue().set(
                "user:" + user.getUsername(),
                userDTO,
                expiration,
                TimeUnit.SECONDS
        );

        // 9. 返回登录响应
        return LoginResponse.builder()
                .token(jwt)
                .user(userDTO)
                .build();
    }

    @Override
    public UserDTO validateUserInfo(ValidateUserRequest request) {

        // 查询用户
        TUser user = this.getOne(new LambdaQueryWrapper<TUser>()
                .eq(TUser::getUsername, request.getUsername())
                .eq(TUser::getIsDeleted, 0));

        // 检查用户是否存在
        if (user == null) {
            log.warn("用户不存在: {}", request.getUsername());
            throw new ServiceException("用户不存在");
        }

        // 检查学号是否匹配
        if (!request.getStudentId().equals(user.getStudentId())) {
            log.warn("学号不匹配: {} vs {}", request.getStudentId(), user.getStudentId());
            throw new ServiceException("学号与用户不匹配");
        }

        // 检查真实姓名是否匹配
        if (!request.getRealName().equals(user.getRealName())) {
            log.warn("真实姓名不匹配: {} vs {}", request.getRealName(), user.getRealName());
            throw new ServiceException("姓名与用户不匹配");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            log.warn("用户已被禁用: {}", request.getUsername());
            throw new ServiceException("账号已被禁用");
        }

        // 转换为DTO并返回
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        return userDTO;
    }

    @Override
    public UserDTO findByUsername(String username) {
        log.debug("根据用户名查找用户: {}", username);

        // 首先检查Redis缓存
        String userKey = "user:" + username;
        UserDTO cachedUserDTO = (UserDTO) redisTemplate.opsForValue().get(userKey);

        if (cachedUserDTO != null) {
            log.debug("从Redis缓存中获取到用户: {}", username);
            return cachedUserDTO;
        }

        // 缓存中不存在，从数据库查询
        TUser user = this.getOne(new LambdaQueryWrapper<TUser>()
                .eq(TUser::getUsername, username)
                .eq(TUser::getIsDeleted, 0));

        if (user == null) {
            log.warn("用户不存在: {}", username);
            return null;
        }

        // 转换为DTO
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        // 更新缓存
        redisTemplate.opsForValue().set(userKey, userDTO, 24, TimeUnit.HOURS);

        return userDTO;
    }

    @Override
    public boolean sendEmailVerificationCode(String username, String email) {
        log.debug("发送邮箱验证码: {} - {}", username, email);

        // 验证用户和邮箱是否匹配
        UserDTO user = findByUsername(username);

        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        // 添加更详细的日志
        log.info("用户邮箱详情: 用户名={}, 邮箱={}, 数据库中邮箱={}", username, email, user.getEmail());

        if (user.getEmail() == null) {
            log.warn("用户{}的邮箱为null", username);
            throw new ServiceException("用户邮箱不存在");
        }

        // 进行字符串比较前检查并记录日志
        boolean emailsMatch = email.equals(user.getEmail());
        log.info("邮箱比较结果: {} vs {} = {}", email, user.getEmail(), emailsMatch);

        if (!emailsMatch) {
            log.warn("邮箱与用户不匹配: {} - {}", username, email);
            throw new ServiceException("邮箱与用户不匹配");
        }

        // 检查是否在短时间内频繁发送
        String rateLimitKey = "email_rate_limit:" + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
            Long ttl = redisTemplate.getExpire(rateLimitKey, TimeUnit.SECONDS);
            log.warn("发送过于频繁: {} - 剩余等待时间: {}秒", email, ttl);
            throw new ServiceException("发送过于频繁，请" + ttl + "秒后再试");
        }

        // 生成验证码
        String verifyCode = verifyCodeService.generateCode();

        // 保存验证码
        verifyCodeService.saveCode(email, verifyCode);

        // 设置频率限制（60秒内不能重复发送）
        redisTemplate.opsForValue().set(rateLimitKey, "1", 60, TimeUnit.SECONDS);

        // 通过RabbitMQ发送验证码邮件
        emailMessageProducer.sendVerificationCodeEmail(email, verifyCode);

        return true;
    }

    @Override
    public boolean sendPhoneVerificationCode(String username, String phone) {
        log.debug("发送手机验证码: {} - {}", username, phone);

        // 验证用户和手机号是否匹配
        UserDTO user = findByUsername(username);

        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        if (!phone.equals(user.getPhone())) {
            log.warn("手机号与用户不匹配: {} - {}", username, phone);
            throw new ServiceException("手机号与用户不匹配");
        }

        // 检查是否在短时间内频繁发送
        String rateLimitKey = "phone_rate_limit:" + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
            Long ttl = redisTemplate.getExpire(rateLimitKey, TimeUnit.SECONDS);
            log.warn("发送过于频繁: {} - 剩余等待时间: {}秒", phone, ttl);
            throw new ServiceException("发送过于频繁，请" + ttl + "秒后再试");
        }

        // 生成验证码
        String verifyCode = verifyCodeService.generateCode();

        // 保存验证码
        verifyCodeService.saveCode(phone, verifyCode);

        // 设置频率限制（60秒内不能重复发送）
        redisTemplate.opsForValue().set(rateLimitKey, "1", 60, TimeUnit.SECONDS);

        // TODO: 调用短信发送服务发送验证码
        // 这里暂时模拟发送成功
        log.info("模拟发送短信验证码到手机: {} - {}", phone, verifyCode);

        return true;
    }

    /**
     * 重置密码
     *
     * @param resetPasswordRequest 重置密码请求
     * @return 是否重置成功
     * @throws ServiceException 如果验证失败或其他错误
     */
    @Override
    public boolean resetPassword(ResetPasswordRequest resetPasswordRequest) {
        log.info("重置密码: {}", resetPasswordRequest.getUsername());

        // 1. 验证用户基本信息
        String username = resetPasswordRequest.getUsername();
        String studentId = resetPasswordRequest.getStudentId();
        String realName = resetPasswordRequest.getRealName();

        // 查询用户
        UserDTO user = findByUsername(username);
        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new ServiceException("用户不存在");
        }

        // 验证学号和真实姓名
        if (!studentId.equals(user.getStudentId())) {
            log.warn("学号不匹配: {} vs {}", studentId, user.getStudentId());
            throw new ServiceException("学号不匹配");
        }

        if (!realName.equals(user.getRealName())) {
            log.warn("真实姓名不匹配: {} vs {}", realName, user.getRealName());
            throw new ServiceException("真实姓名不匹配");
        }

        // 2. 验证联系方式和验证码
        String contactType = resetPasswordRequest.getContactType();
        String contact = resetPasswordRequest.getContact();
        String verifyCode = resetPasswordRequest.getVerifyCode();

        // 使用修改后的verifyCode方法验证
        boolean isValid;
        if ("email".equals(contactType)) {
            if (!contact.equals(user.getEmail())) {
                log.warn("邮箱不匹配: {} vs {}", contact, user.getEmail());
                throw new ServiceException("邮箱不匹配");
            }

            // 验证邮箱验证码 - 使用verifyCodeService的方法，它已被修改为支持已验证状态
            isValid = verifyCodeService.verifyCode(contact, verifyCode);
        } else if ("phone".equals(contactType)) {
            if (!contact.equals(user.getPhone())) {
                log.warn("手机号不匹配: {} vs {}", contact, user.getPhone());
                throw new ServiceException("手机号不匹配");
            }

            // 验证手机验证码 - 使用verifyCodeService的方法，它已被修改为支持已验证状态
            isValid = verifyCodeService.verifyCode(contact, verifyCode);
        } else {
            log.warn("不支持的联系方式类型: {}", contactType);
            throw new ServiceException("不支持的联系方式类型");
        }

        if (!isValid) {
            log.warn("验证码验证失败: {}", contact);
            throw new ServiceException("验证码错误或已过期");
        }

        // 3. 更新密码
        String newPassword = resetPasswordRequest.getNewPassword();
        // 使用BCrypt加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);

        // 获取实体对象
        TUser userEntity = getById(user.getId());
        if (userEntity == null) {
            log.error("用户实体不存在: {}", username);
            throw new ServiceException("用户不存在");
        }

        // 更新密码
        userEntity.setPassword(encodedPassword);
        // 更新时间
        userEntity.setUpdateTime(new Date());

        // 保存到数据库
        boolean updated = updateById(userEntity);
        if (!updated) {
            log.error("更新密码失败: {}", username);
            throw new ServiceException("更新密码失败");
        }

        // 清除用户缓存
        clearUserCache(username);

        log.info("密码重置成功: {}", username);
        return true;
    }

    /**
     * 清除用户缓存
     *
     * @param username 用户名
     */
    private void clearUserCache(String username) {
        if (username == null || username.isEmpty()) {
            log.debug("清除用户缓存时用户名为空");
            return;
        }

        log.info("清除用户缓存: {}", username);

        try {
            // 获取用户信息
            UserDTO cachedUser = (UserDTO) redisTemplate.opsForValue().get("user:" + username);

            if (cachedUser != null) {
                // 删除账号映射缓存
                if (cachedUser.getEmail() != null) {
                    redisTemplate.delete("account:email:" + cachedUser.getEmail());
                    log.debug("删除邮箱映射缓存: {}", cachedUser.getEmail());
                }

                if (cachedUser.getPhone() != null) {
                    redisTemplate.delete("account:phone:" + cachedUser.getPhone());
                    log.debug("删除手机号映射缓存: {}", cachedUser.getPhone());
                }

                if (cachedUser.getStudentId() != null) {
                    redisTemplate.delete("account:studentId:" + cachedUser.getStudentId());
                    log.debug("删除学号映射缓存: {}", cachedUser.getStudentId());
                }

                // 删除用户名映射缓存
                redisTemplate.delete("account:username:" + username);
            }

            // 删除用户信息和令牌缓存
            redisTemplate.delete("user:" + username);
            redisTemplate.delete("token:" + username);

            log.info("用户缓存清除完成: {}", username);
        } catch (Exception e) {
            log.error("清除用户缓存时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 更新用户密码
     *
     * @param updatePasswordRequest 更新密码请求
     * @return 是否更新成功
     * @throws ServiceException 如果验证失败或其他错误
     */
    @Override
    public boolean updatePassword(UpdatePasswordRequest updatePasswordRequest) {
        log.info("更新密码: {}", updatePasswordRequest.getUsername());

        // 1. 验证用户是否存在
        String username = updatePasswordRequest.getUsername();

        // 查询用户
        UserDTO user = findByUsername(username);
        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new ServiceException("用户不存在");
        }

        // 2. 获取用户实体，验证原密码
        TUser userEntity = getById(user.getId());
        if (userEntity == null) {
            log.error("用户实体不存在: {}", username);
            throw new ServiceException("用户不存在");
        }

        // 验证原密码
        if (!passwordEncoder.matches(updatePasswordRequest.getOldPassword(), userEntity.getPassword())) {
            log.warn("原密码错误: {}", username);
            throw new ServiceException("原密码错误");
        }

        // 3. 更新密码
        String newPassword = updatePasswordRequest.getNewPassword();
        // 使用BCrypt加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);

        // 更新密码
        userEntity.setPassword(encodedPassword);
        // 更新时间
        userEntity.setUpdateTime(new Date());

        // 保存到数据库
        boolean updated = updateById(userEntity);
        if (!updated) {
            log.error("更新密码失败: {}", username);
            throw new ServiceException("更新密码失败");
        }

        // 4. 清除用户缓存
        clearUserCache(username);

        log.info("密码更新成功: {}", username);
        return true;
    }

    /**
     * 更新用户信息
     *
     * @param updateUserInfoRequest 更新用户信息请求
     * @return 更新后的用户信息
     * @throws ServiceException 如果更新失败或其他错误
     */
    @Override
    public UserDTO updateUserInfo(UpdateUserInfoRequest updateUserInfoRequest) {
        log.info("更新用户信息: ID={}, 用户名={}", updateUserInfoRequest.getId(), updateUserInfoRequest.getUsername());

        // 1. 根据ID查询用户是否存在
        TUser userEntity = getById(updateUserInfoRequest.getId());
        if (userEntity == null) {
            log.error("用户不存在: ID={}", updateUserInfoRequest.getId());
            throw new ServiceException("用户不存在");
        }

        // 2. 检查用户名是否已被其他用户使用 (如果用户名有变更)
        String newUsername = updateUserInfoRequest.getUsername();
        String oldUsername = userEntity.getUsername();
        boolean usernameChanged = !newUsername.equals(oldUsername);

        if (usernameChanged) {
            if (this.count(new LambdaQueryWrapper<TUser>()
                    .eq(TUser::getUsername, newUsername)
                    .ne(TUser::getId, userEntity.getId())
                    .eq(TUser::getIsDeleted, 0)) > 0) {
                log.warn("用户名已存在: {}", newUsername);
                throw new ServiceException("用户名已存在");
            }

            // 在更新用户名前清除旧用户名的缓存
            log.info("用户名变更，清除旧用户名缓存: {} -> {}", oldUsername, newUsername);
            clearUserCache(oldUsername);

            // 更新用户名
            userEntity.setUsername(newUsername);
        }

        // 3. 更新头像 (如果提供)
        String avatar = updateUserInfoRequest.getAvatar();
        if (StringUtils.hasText(avatar)) {
            userEntity.setAvatar(avatar);
        }

        // 5. 更新时间
        userEntity.setUpdateTime(new Date());

        // 6. 保存到数据库
        boolean updated = updateById(userEntity);
        if (!updated) {
            log.error("更新用户信息失败: ID={}", updateUserInfoRequest.getId());
            throw new ServiceException("更新用户信息失败");
        }

        // 7. 如果用户名没有变更，直接更新缓存中的用户信息，无需清除
        if (!usernameChanged) {
            // 直接获取缓存中的用户信息并更新头像
            // 无需完全清除缓存，只需要更新缓存中存储的用户信息
            UserDTO cachedUserDTO = (UserDTO) redisTemplate.opsForValue().get("user:" + oldUsername);
            if (cachedUserDTO != null) {
                cachedUserDTO.setAvatar(userEntity.getAvatar());
                redisTemplate.opsForValue().set(
                        "user:" + oldUsername,
                        cachedUserDTO,
                        24 * 60 * 60,
                        TimeUnit.SECONDS
                );
                // 直接返回更新后的用户信息，不需要重新从数据库读取
                return cachedUserDTO;
            }
        }

        // 8. 返回更新后的用户信息
        UserDTO updatedUserDTO = new UserDTO();
        BeanUtils.copyProperties(userEntity, updatedUserDTO);

        // 9. 更新Redis缓存 - 使用新用户名
        redisTemplate.opsForValue().set(
                "user:" + newUsername,
                updatedUserDTO,
                24 * 60 * 60,
                TimeUnit.SECONDS
        );

        // 10. 为新用户名添加账号映射缓存
        if (usernameChanged) {
            redisTemplate.opsForValue().set(
                    "account:username:" + newUsername,
                    newUsername,
                    24 * 60 * 60,
                    TimeUnit.SECONDS
            );
        }
        log.info("用户信息更新成功: ID={}, 用户名={}", updateUserInfoRequest.getId(), newUsername);
        return updatedUserDTO;
    }

    /**
     * 用户退出登录
     * 清除Redis中的token和用户信息缓存
     *
     * @param username 用户名
     * @return 是否退出成功
     */
    @Override
    public boolean logout(String username) {
        log.info("用户退出登录: {}", username);

        if (username == null || username.isEmpty()) {
            log.warn("退出登录时用户名为空");
            return false;
        }

        try {
            // 清除用户所有缓存
            clearUserCache(username);

            log.info("用户退出登录成功: {}", username);
            return true;
        } catch (Exception e) {
            log.error("退出登录失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送新邮箱验证码
     *
     * @param username 用户名
     * @param newEmail 新邮箱
     * @return 发送结果，true表示成功，false表示失败
     * @throws ServiceException 如果用户不存在或其他错误
     */
    @Override
    public boolean sendNewEmailVerificationCode(String username, String newEmail) {
        log.debug("发送新邮箱验证码: {} - {}", username, newEmail);

        // 验证用户是否存在
        UserDTO user = findByUsername(username);

        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        // 检查新邮箱是否与当前邮箱相同
        if (user.getEmail() != null && user.getEmail().equals(newEmail)) {
            throw new ServiceException("新邮箱不能与当前邮箱相同");
        }

        // 检查新邮箱是否已被其他用户使用
        if (this.count(new LambdaQueryWrapper<TUser>()
                .eq(TUser::getEmail, newEmail)
                .ne(TUser::getUsername, username)
                .eq(TUser::getIsDeleted, 0)) > 0) {
            throw new ServiceException("该邮箱已被其他用户使用");
        }

        // 检查是否在短时间内频繁发送
        String rateLimitKey = "email_rate_limit:" + newEmail;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
            Long ttl = redisTemplate.getExpire(rateLimitKey, TimeUnit.SECONDS);
            log.warn("发送过于频繁: {} - 剩余等待时间: {}秒", newEmail, ttl);
            throw new ServiceException("发送过于频繁，请" + ttl + "秒后再试");
        }

        // 生成验证码
        String verifyCode = verifyCodeService.generateCode();

        // 保存验证码
        verifyCodeService.saveCode(newEmail, verifyCode);

        // 设置频率限制（60秒内不能重复发送）
        redisTemplate.opsForValue().set(rateLimitKey, "1", 60, TimeUnit.SECONDS);

        // 通过RabbitMQ发送验证码邮件
        emailMessageProducer.sendVerificationCodeEmail(newEmail, verifyCode);

        return true;
    }

    /**
     * 更新用户邮箱
     *
     * @param request 更新邮箱请求
     * @return 更新后的用户信息
     * @throws ServiceException 如果更新失败或其他错误
     */
    @Override
    public UserDTO updateUserEmail(UpdateEmailRequest request) {
        log.info("更新用户邮箱: ID={}, 用户名={}, 新邮箱={}", request.getUserId(), request.getUsername(), request.getNewEmail());

        // 1. 根据ID查询用户是否存在
        TUser userEntity = getById(request.getUserId());
        if (userEntity == null) {
            log.error("用户不存在: ID={}", request.getUserId());
            throw new ServiceException("用户不存在");
        }

        // 2. 检查用户名是否匹配
        if (!userEntity.getUsername().equals(request.getUsername())) {
            log.error("用户名不匹配: {} vs {}", request.getUsername(), userEntity.getUsername());
            throw new ServiceException("用户名不匹配");
        }

        // 3. 检查邮箱是否与当前邮箱相同
        if (userEntity.getEmail() != null && userEntity.getEmail().equals(request.getNewEmail())) {
            log.warn("新邮箱与当前邮箱相同: {}", request.getNewEmail());
            throw new ServiceException("新邮箱不能与当前邮箱相同");
        }

        // 4. 检查新邮箱是否已被其他用户使用
        if (this.count(new LambdaQueryWrapper<TUser>()
                .eq(TUser::getEmail, request.getNewEmail())
                .ne(TUser::getId, request.getUserId())
                .eq(TUser::getIsDeleted, 0)) > 0) {
            log.warn("邮箱已被其他用户使用: {}", request.getNewEmail());
            throw new ServiceException("该邮箱已被其他用户使用，请使用其他邮箱");
        }

        // 5. 验证验证码
        boolean isValid = verifyCodeService.verifyCode(request.getNewEmail(), request.getVerifyCode());
        if (!isValid) {
            log.warn("验证码验证失败: {}", request.getNewEmail());
            throw new ServiceException("验证码错误或已过期");
        }

        // 6. 保存旧邮箱（用于清除缓存）
        String oldEmail = userEntity.getEmail();

        // 7. 更新邮箱
        userEntity.setEmail(request.getNewEmail());
        userEntity.setUpdateTime(new Date());

        // 8. 保存到数据库
        boolean updated = updateById(userEntity);
        if (!updated) {
            log.error("更新用户邮箱失败: ID={}", request.getUserId());
            throw new ServiceException("更新用户邮箱失败");
        }

        // 9. 清除缓存
        clearUserCache(userEntity.getUsername());

        // 10. 如果有旧邮箱，清除邮箱映射
        if (StringUtils.hasText(oldEmail)) {
            redisTemplate.delete("account:email:" + oldEmail);
        }

        // 11. 添加新邮箱映射
        redisTemplate.opsForValue().set(
                "account:email:" + request.getNewEmail(),
                userEntity.getUsername(),
                24 * 60 * 60,
                TimeUnit.SECONDS
        );

        // 12. 返回更新后的用户信息
        UserDTO updatedUserDTO = new UserDTO();
        BeanUtils.copyProperties(userEntity, updatedUserDTO);

        // 13. 更新Redis缓存
        redisTemplate.opsForValue().set(
                "user:" + userEntity.getUsername(),
                updatedUserDTO,
                24 * 60 * 60,
                TimeUnit.SECONDS
        );

        log.info("用户邮箱更新成功: ID={}, 用户名={}, 新邮箱={}", request.getUserId(), request.getUsername(), request.getNewEmail());
        return updatedUserDTO;
    }
}

