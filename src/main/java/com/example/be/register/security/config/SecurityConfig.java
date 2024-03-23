package com.example.be.register.security.config;

import com.example.be.register.security.UserDetailsService.UserDetailServiceImplByPhone;
import com.example.be.register.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

import javax.servlet.DispatcherType;
import java.security.KeyStore;
import java.util.Collections;
import java.util.List;

/**
 * @author : zxm
 * @date: 2024/3/21 - 16:01
 * @Description: com.example.be.register.security.config
 * @version: 1.0
 */
//@EnableWebSecurity  // 包含了@Configuration 和 @springSecurityFilterChain
//@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@Configuration
public class SecurityConfig {

    @Autowired
    UserDetailServiceImplByPhone userDetailServiceImplByPhone;

    @Bean
    UserDetailServiceImplByPhone customUserDetailsService() {
        return new UserDetailServiceImplByPhone();
    }

    @Bean
    AuthenticationManager authenticationManager(UserDetailServiceImplByPhone userDetailServiceImplByPhone,
                                                PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailServiceImplByPhone);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        ProviderManager pm = new ProviderManager(daoAuthenticationProvider);
        return pm;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        http
                // CSRF禁用，因为不使用session
                .csrf().disable().sessionManagement()
                //基于token，所以不需要session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http
                //过滤请求
                .authorizeHttpRequests()
                // 对于登录login 验证码code 允许匿名访问
//                .antMatchers("/user/code","/user/login").permitAll()
                .requestMatchers("/user/code","/user/login").permitAll()
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated();

        // TODO 错误页面，跨域。。。。
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}