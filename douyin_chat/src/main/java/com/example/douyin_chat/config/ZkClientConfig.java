package com.example.douyin_chat.config;

import com.example.douyin_chat.distributed.zookeeper.CuratorZKclient;
import com.example.douyin_chat.util.SpringContextUtil;
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

    @Value("zookerper.connect.url")
    private String zkConnect;

    @Value("${zookeeper.connect.SessionTimeout}")
    private String zkSessionTimeout;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.setContext(applicationContext);
    }

    @Bean
    public CuratorZKclient curatorZKclient(){
        return new CuratorZKclient(zkConnect, zkSessionTimeout);
    }


}
