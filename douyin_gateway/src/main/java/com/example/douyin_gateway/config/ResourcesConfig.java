package com.example.douyin_gateway.config;



import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.util.pattern.PathPatternParser;

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
    public CorsWebFilter corsFilter()
    {
        CorsConfiguration config = new CorsConfiguration();

        // 设置访问源地址
//        config.setAllowedOriginPatterns(Collections.singletonList("http://localhost:8080"));
        config.setAllowedOriginPatterns(Collections.singletonList("*"));

        // 设置访问源请求头
        config.addAllowedHeader(CorsConfiguration.ALL);

        // 设置访问源请求方法
        config.addAllowedMethod(CorsConfiguration.ALL);

        // 允许凭证
        config.setAllowCredentials(true);

        config.setMaxAge(18000L);

        // 对接口配置跨域设置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}