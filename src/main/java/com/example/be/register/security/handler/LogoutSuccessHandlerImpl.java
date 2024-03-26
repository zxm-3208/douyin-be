package com.example.be.register.security.handler;

import com.example.be.common.constant.Constants;
import com.example.be.register.domain.dto.LoginUserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author : zxm
 * @date: 2024/3/26 - 17:01
 * @Description: com.example.be.register.security.handler
 * @version: 1.0
 */
@Configuration
@Slf4j
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//        // 获取当前用户的认证信息
//        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
//        log.info("xxxx{}",authenticationToken);
//        if(Objects.isNull(authenticationToken)){
//            throw new RuntimeException("获取用户认证信息失败，请重新登录！");
//        }
//        LoginUserDTO loginUserDTO = (LoginUserDTO) authenticationToken.getPrincipal();
//        String userId = loginUserDTO.getToken();
//        // 删除Redis中的用户信息
//        redisTemplate.delete(Constants.LOGIN_TOKEN_KEY + userId);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("msg", "注销成功");
        result.put("status", 200);
        response.setContentType("application/json;charset=UTF-8");
        String s = new ObjectMapper().writeValueAsString(result);

        log.info("注销成功");

        response.getWriter().println(s);
    }
}
