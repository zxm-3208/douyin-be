package com.example.douyin_chat.distributed.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @author : zxm
 * @date: 2024/6/2 - 19:49
 * @Description: com.example.douyin_chat.distributed.zookeeper
 * @version: 1.0
 */
public class ClientFactory {
    
    /**
     * zookeeper的连接地址
     */
    public static CuratorFramework createSimple(String connectionString, String timeout){
        // 每次重试等待时间翻倍
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);

        // zk的连接地址，会话超时时间，连接超时时间，重试策略
        return CuratorFrameworkFactory.newClient(connectionString, Integer.parseInt(timeout), Integer.parseInt(timeout), retryPolicy);
    }

    public static CuratorFramework createWithOptions(String connectionString, RetryPolicy retryPolicy, int connectionTimeoutMs, int sessionTimeoutMs){
        // builder模式创建CurtorFramework实例
        return CuratorFrameworkFactory.builder()
                .connectionTimeoutMs(connectionTimeoutMs)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(sessionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .build();
    }
    
}
