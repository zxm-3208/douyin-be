package com.example.be.register.domain.dto;

import com.example.be.register.domain.po.DyUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * @author : zxm
 * @date: 2024/3/27 - 16:11
 * @Description: com.example.be.register.domain.dto
 * @version: 1.0
 */
@Data
public class LoginUserDTO implements UserDetails {
    private String token;
    private Long loginTime;
    private Long expireTime;
    private DyUser dyUser;

    public LoginUserDTO(DyUser dyUser) {
        this.dyUser = dyUser;
    }

    public LoginUserDTO() {
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        if (dyUser != null)
            return dyUser.getPassword();
        else
            return null;
    }

    @Override
    public String getUsername() {
        if(dyUser!=null)
            return dyUser.getUserName();
        else
            return null;
    }

    // 用于判断用户的账号是否未过期，可以实现账户的有效控制
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 用于判断用户的账户是否未锁定，可以用于实现账户锁定功能。
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 用于判断用户的凭证（如密码）是否未过期，可以用于实现密码有效期控制
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 用于判断用户是否已激活，可以用于实现账户激活功能。
    @Override
    public boolean isEnabled() {
        return true;
    }
}
