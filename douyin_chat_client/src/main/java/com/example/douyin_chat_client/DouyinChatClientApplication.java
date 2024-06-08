package com.example.douyin_chat_client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableDiscoveryClient
//@SpringBootApplication
@EnableFeignClients
//@MapperScan("com.example.douyin_chat.mapper.**")
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)		// 不配置数据源
public class DouyinChatClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(DouyinChatClientApplication.class, args);
    }

}
