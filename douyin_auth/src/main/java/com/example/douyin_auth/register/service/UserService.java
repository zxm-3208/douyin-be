package com.example.douyin_auth.register.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.douyin_auth.register.domain.po.DyUser;
import com.example.douyin_auth.register.domain.vo.PhoneLoginUserVO;
import com.example.douyin_auth.register.domain.vo.UserNameLoginUserVo;
import com.example.douyin_commons.core.domain.BaseResponse;
import org.springframework.stereotype.Service;

/**
 * @author : zxm
 * @date: 2024/3/20 - 15:42
 * @Description: com.example.be.register.service.impl
 * @version: 1.0
 */
@Service
public interface UserService extends IService<DyUser> {
    BaseResponse send(String phone);

    BaseResponse getCode();

    BaseResponse login(PhoneLoginUserVO phoneLoginUserVO);

    BaseResponse loginByUserName(UserNameLoginUserVo userNameLoginUserVo);

//    BaseResponse logout();
}
