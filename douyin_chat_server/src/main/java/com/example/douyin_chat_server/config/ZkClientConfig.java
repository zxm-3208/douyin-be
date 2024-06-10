package com.example.douyin_chat_server.config;


import com.example.douyin_chat_commons.distributed.CuratorZKclient;
import com.example.douyin_chat_commons.util.SpringContextUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : zxm
 * @date: 2024/6/3 - 14:40
 * @Description: com.example.douyin_chat.config
 * @version: 1.0
 */
@Configuration
public class ZkClientConfig implements ApplicationContextAware {

    @Value("${zookeeper.connect.url}")
    private String zkConnect;

    @Value("${zookeeper.connect.SessionTimeout}")
    private String zkSessionTimeout;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        SpringContextUtil.setContext(applicationContext);
    }


    @Bean(name = "curatorZKClient")
    public CuratorZKclient curatorZKClient()
    {
        return new CuratorZKclient(zkConnect,zkSessionTimeout);
    }
    
}

