package com.auditStudyHub.security;

import com.auditStudyHub.entity.TUser;
import com.auditStudyHub.mapper.TUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final TUserMapper tUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 使用 MyBatis-Plus 的 LambdaQueryWrapper 查询用户
        TUser user = tUserMapper.selectOne(new LambdaQueryWrapper<TUser>()
                .eq(TUser::getUsername, username)
                .eq(TUser::getIsDeleted, 0));
        
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        // 创建权限列表
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole() == 1 ? "ROLE_ADMIN" : "ROLE_USER"));
        
        // 返回UserDetails对象
        return new User(
            user.getUsername(),
            user.getPassword(),
            user.getStatus() == 1, // enabled
            true, // accountNonExpired
            true, // credentialsNonExpired
            true, // accountNonLocked
            authorities
        );
    }
}