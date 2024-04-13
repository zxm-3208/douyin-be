package com.example.douyin_publish.config;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : zxm
 * @date: 2024/4/11 - 10:52
 * @Description: com.example.douyin_publish.config
 * @version: 1.0
 */
@Configuration
public class IdConfig {
    @Bean
    SnowflakeGenerator snowflakeGenerator(){
        // 如果是分布式，WorkerId需要唯一
        return new SnowflakeGenerator(1, 0);
    }
}
