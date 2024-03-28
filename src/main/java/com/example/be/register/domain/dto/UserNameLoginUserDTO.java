package com.example.be.register.domain.dto;

import com.example.be.register.domain.po.DyUser;
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
public class UserNameLoginUserDTO extends LoginUserDTO {

    public UserNameLoginUserDTO(DyUser dyUser) {
        this.setDyUser(dyUser);
    }

    public UserNameLoginUserDTO() {
    }

}
