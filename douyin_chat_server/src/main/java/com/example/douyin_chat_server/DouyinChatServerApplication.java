package com.example.douyin_chat_server;

import com.example.douyin_chat_server.session.service.SessionManger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableDiscoveryClient
@EnableFeignClients
//@MapperScan("com.example.douyin_chat.mapper.**")
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)		// 不配置数据源
public class DouyinChatServerApplication {

    public static void main(String[] args) {
        // 启动并初始化 Spring 环境及其各 Spring 组件
        ApplicationContext context = SpringApplication.run(DouyinChatServerApplication.class, args);

        /**
         * 将SessionManger 单例设置为spring bean
         */
        SessionManger sessionManger = context.getBean(SessionManger.class);
        SessionManger.setSingleInstance(sessionManger);

        // 启动服务
        ChatServer nettyServer = context.getBean(ChatServer.class);
        nettyServer.run();
    }

}
