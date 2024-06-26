package com.example.douyin_auth.register.controller;

import com.example.douyin_auth.register.domain.vo.PhoneLoginUserVO;
import com.example.douyin_auth.register.domain.vo.UserNameLoginUserVo;
import com.example.douyin_auth.register.service.UserService;
import com.example.douyin_commons.core.domain.BaseResponse;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送手机验证码
     */
    @GetMapping("/code")
    public BaseResponse sendCode(@RequestParam String phone){
        return userService.send(phone);
    }

    /**
     * 图形验证码
     */
    @GetMapping("/captchaImage")
    public BaseResponse getCode(){
        return userService.getCode();
    }

    /**
     * 手机号+验证码
     * @param phoneLoginUserVO 登录参数，包含phone和code
     */
    @PostMapping("/login")
    public BaseResponse login(@RequestBody PhoneLoginUserVO phoneLoginUserVO){
        return userService.login(phoneLoginUserVO);
    }

    /**
     *  用户名+密码+验证码
     */
    @PostMapping("/logoinbyusername")
    public BaseResponse loginByUserName(@RequestBody UserNameLoginUserVo userNameLoginUserVo){
        return userService.loginByUserName(userNameLoginUserVo);
    }


//    @GetMapping("/logout")
//    public BaseResponse logout(){
//        return userService.logout();
//    }

    @GetMapping("/hello")
    public void hello(){
        log.info("hello===============");
    }

}
