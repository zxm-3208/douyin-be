package com.example.douyin_commons;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)		// common服务不需要配置数据源
public class DouyinCommonsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DouyinCommonsApplication.class, args);
    }

}
