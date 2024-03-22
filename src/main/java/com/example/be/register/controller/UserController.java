package com.example.be.register.controller;

import com.example.be.common.core.domain.BaseResponse;
import com.example.be.register.domain.vo.LoginUserVO;
import com.example.be.register.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author : zxm
 * @date: 2024/3/19 - 21:10
 * @Description: com.example.be.register.controller
 * @version: 1.0
 */

@RestController     //@Controller + @ResponseBody
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送手机验证码
     */
    @PostMapping("/code")
    public BaseResponse sendCode(@RequestParam String phone){
        // TODO 发送短信验证码并保存验证
        return userService.send(phone);
    }

    /**
     * 验证，登录/注册
     * @param loginUserVO 登录参数，包含phone和code 或 phone和密码
     */
    @PostMapping("/login")
    public BaseResponse login(@RequestBody LoginUserVO loginUserVO){
        return userService.login(loginUserVO);
    }

}
