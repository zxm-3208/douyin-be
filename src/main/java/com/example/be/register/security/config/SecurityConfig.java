package com.example.be.register.security.config;

import com.example.be.register.security.UserDetailsService.UserDetailServiceImplByPhone;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author : zxm
 * @date: 2024/3/21 - 16:01
 * @Description: com.example.be.register.security.config
 * @version: 1.0
 */
@EnableWebSecurity  // 包含了@Configuration 和 @springSecurityFilterChain
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public SecurityFilterChain authenticationManager(HttpSecurity http, UserDetailServiceImplByPhone userDetailServiceImplByPhone)
            throws Exception {
        http
                // CSRF禁用，因为不使用session
                .csrf().disable().sessionManagement()
                //基于token，所以不需要session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http
                //过滤请求
                .authorizeRequests()
                // 对于登录login 验证码captchaImage 允许匿名访问
                .mvcMatchers("/user/code","/user/login").anonymous()
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated();

        // TODO 错误页面，跨域。。。。
        return http.build();
    }


}