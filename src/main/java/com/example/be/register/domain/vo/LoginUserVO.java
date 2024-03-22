package com.example.be.register.domain.vo;

import lombok.Data;

/**
 * @author : zxm
 * @date: 2024/3/21 - 17:10
 * @Description: com.example.be.register.domain.vo
 * @version: 1.0
 */
@Data
public class LoginUserVO {
    private String phone;
    private String code;
    private String password;
    private String userName;
}
