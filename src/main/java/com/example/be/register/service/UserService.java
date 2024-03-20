package com.example.be.register.service;

import com.example.be.common.core.domain.BaseResponse;

/**
 * @author : zxm
 * @date: 2024/3/20 - 15:42
 * @Description: com.example.be.register.service.impl
 * @version: 1.0
 */
public interface UserService {
    BaseResponse send(String phone);
}
