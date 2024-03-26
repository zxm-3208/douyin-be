package com.example.be.register.security.config;

import com.example.be.register.security.filterf.JwtAuthenticationTokenFilter;
import com.example.be.register.security.service.impl.UserDetailServiceImplByPhone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

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

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private LogoutSuccessHandler logoutSuccessHandler;

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
                .requestMatchers("/user/code","/user/login").permitAll()
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated();

        // 将自定义认证过滤器添加到过滤器链中
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        // 配置异常处理器
        http.exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint);    // 配置认证失败处理器

        // 自定义注销登录处理器
        http.logout()
//                .logoutUrl("/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutSuccessHandler(logoutSuccessHandler);
//                .logoutSuccessUrl("http://localhost:8080/sign");

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}