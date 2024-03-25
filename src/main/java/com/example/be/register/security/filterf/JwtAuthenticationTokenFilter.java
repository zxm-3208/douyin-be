package com.example.be.register.security.filterf;

import com.example.be.register.domain.dto.LoginUserDTO;
import com.example.be.register.security.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * @author : zxm
 * @date: 2024/3/25 - 9:52
 * @Description: 自定义认证过滤器，用来校验用户请求中携带的Token
 * @version: 1.0
 */

// 继承OncePerRequestFilter可以简化过滤器编写，并确保每个请求只被过滤一次，避免多次过滤的问题。
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    // OncePerRequestFilter doFilter方法第一次执行的时候会执行doFilterInternal
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        LoginUserDTO loginUser = tokenService.getLoginUserDTO(request);

        // 2. 在SecurityContextHolder中获取用户认证对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(!Objects.isNull(loginUser)&&Objects.isNull(authentication)) {
            // Token剩余时间小于XX，则刷新
            tokenService.verifyToken(loginUser);

            // 保存用户认证信息
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        // 放行
        filterChain.doFilter(request, response);
    }
}
