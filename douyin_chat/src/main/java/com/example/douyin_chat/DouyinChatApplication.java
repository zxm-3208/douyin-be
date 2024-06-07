package com.example.douyin_chat;

import com.example.douyin_chat.server.ChatServer;
import com.example.douyin_chat.server.session.service.SessionManger;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableDiscoveryClient
//@SpringBootApplication
@EnableFeignClients
//@MapperScan("com.example.douyin_chat.mapper.**")
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)		// 网关服务不需要配置数据源
public class DouyinChatApplication {

	@Autowired
	static ChatServer chatServer;

	public static void main(String[] args)
	{
		// 启动并初始化 Spring 环境及其各 Spring 组件
		ApplicationContext context =
				SpringApplication.run(DouyinChatApplication.class, args);

		// 启动服务
		chatServer.run();


	}

}
