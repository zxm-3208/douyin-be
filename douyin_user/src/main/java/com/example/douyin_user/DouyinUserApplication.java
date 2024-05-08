package com.example.douyin_user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
@MapperScan("com.example.douyin_user.mapper.**")
@EnableAspectJAutoProxy(exposeProxy = true)
public class DouyinUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(DouyinUserApplication.class, args);
    }

}
