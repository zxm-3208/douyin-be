package com.example.douyin_auth.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

/**
 * @author : zxm
 * @date: 2024/3/20 - 21:14
 * @Description: com.example.be.common.config
 * @version: 1.0
 */
@Configuration
public class ResourcesConfig {
    /**
     * 跨域配置
     */
    @Bean
    public CorsFilter corsFilter()
    {
        CorsConfiguration config = new CorsConfiguration();

        // 设置访问源地址
        config.setAllowedOriginPatterns(Collections.singletonList("http://localhost:8080"));

        // 设置访问源请求头
        config.addAllowedHeader(CorsConfiguration.ALL);

        // 设置访问源请求方法
        config.addAllowedMethod(CorsConfiguration.ALL);

        // 允许凭证
        config.setAllowCredentials(true);

        // 对接口配置跨域设置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}