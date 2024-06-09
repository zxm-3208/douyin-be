package com.example.douyin_chat_gate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableDiscoveryClient
//@SpringBootApplication
@EnableFeignClients
@MapperScan("com.example.douyin_chat_gate.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class DouyinChatGateApplication {

    public static void main(String[] args) {
        SpringApplication.run(DouyinChatGateApplication.class, args);
    }

}
