package com.example.be.register.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.be.common.core.domain.BaseResponse;
import com.example.be.register.domain.po.DyUser;
import com.example.be.register.domain.vo.LoginUserVO;

/**
 * @author : zxm
 * @date: 2024/3/20 - 15:42
 * @Description: com.example.be.register.service.impl
 * @version: 1.0
 */
public interface UserService extends IService<DyUser> {
    BaseResponse send(String phone);

    BaseResponse login(LoginUserVO loginUserVO);
}
