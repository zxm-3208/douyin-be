package com.example.douyin_auth.register.domain.vo;

import lombok.Data;

/**
 * @author : zxm
 * @date: 2024/3/27 - 11:34
 * @Description: com.example.be.register.domain.vo
 * @version: 1.0
 */
@Data
public class UserNameLoginUserVo {
    private String userName;

    private String password;

    private String code;

    private String uuid;
}
