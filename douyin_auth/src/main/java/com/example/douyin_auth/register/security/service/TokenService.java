package com.example.douyin_auth.register.security.service;

import com.example.douyin_auth.register.domain.dto.LoginUserDTO;
import com.example.douyin_auth.register.domain.dto.PhoneLoginUserDTO;
import com.example.douyin_auth.register.domain.dto.UserNameLoginUserDTO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author : zxm
 * @date: 2024/3/22 - 10:24
 * @Description: com.example.be.register.security.service
 * @version: 1.0
 */
public interface TokenService {
    String createToken(PhoneLoginUserDTO phoneLoginUserDTO);

    String createToken(UserNameLoginUserDTO userNameLoginUserDTO);

    String refreshToken(LoginUserDTO loginUserDTO);

//    String refreshToken(UserNameLoginUserDTO userNameLoginUserDTO);

    LoginUserDTO getLoginUserDTO(HttpServletRequest request);

    void verifyToken(PhoneLoginUserDTO phoneLoginUserDTO);

}
