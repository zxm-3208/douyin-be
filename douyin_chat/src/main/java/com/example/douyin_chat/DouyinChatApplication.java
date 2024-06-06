package com.example.douyin_chat;

import com.example.douyin_chat.server.ChatServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
@MapperScan("com.example.douyin_chat.mapper.**")
@EnableAspectJAutoProxy(exposeProxy = true)
public class DouyinChatApplication {

	@Autowired
	static ChatServer chatServer;

	public static void main(String[] args) {
		SpringApplication.run(DouyinChatApplication.class, args);

		chatServer.run();
	}

}
