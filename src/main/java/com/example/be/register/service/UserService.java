package com.example.be.register.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.be.common.core.domain.BaseResponse;
import com.example.be.register.domain.po.DyUser;
import com.example.be.register.domain.vo.PhoneLoginUserVO;
import com.example.be.register.domain.vo.UserNameLoginUserVo;
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

    BaseResponse getCode(String userName);

    BaseResponse login(PhoneLoginUserVO phoneLoginUserVO);

    BaseResponse loginByUserName(UserNameLoginUserVo userNameLoginUserVo);

//    BaseResponse logout();
}
