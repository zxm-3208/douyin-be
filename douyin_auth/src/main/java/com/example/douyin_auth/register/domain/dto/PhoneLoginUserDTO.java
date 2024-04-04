package com.example.douyin_auth.register.domain.dto;

import com.example.douyin_auth.register.domain.po.DyUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * @author : zxm
 * @date: 2024/3/20 - 11:24
 * @Description: com.example.be.register.domain.dto
 * @version: 1.0
 */
@Data
public class PhoneLoginUserDTO extends LoginUserDTO {

    public PhoneLoginUserDTO(DyUser dyUser) {
        this.setDyUser(dyUser);
    }

    public PhoneLoginUserDTO() {
    }

    // 用于获取用户被授予的权限，可以用于实现访问控制

    @Override
    public String getPassword() {
        if(this.getDyUser()!=null)
            return this.getDyUser().getCode();
        else
            return null;
    }

    @Override
    public String getUsername() {
        if(this.getDyUser()!=null)
            return this.getDyUser().getPhone();
        else
            return null;
    }


}
