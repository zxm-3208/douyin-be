package com.example.be.register.security.service;

import com.example.be.register.domain.dto.LoginUserDTO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author : zxm
 * @date: 2024/3/22 - 10:24
 * @Description: com.example.be.register.security.service
 * @version: 1.0
 */
public interface TokenService {
    String createToken(LoginUserDTO loginUserDTO);

    void refreshToken(LoginUserDTO loginUserDTO);

    LoginUserDTO getLoginUserDTO(HttpServletRequest request);

    void verifyToken(LoginUserDTO loginUserDTO);

}
