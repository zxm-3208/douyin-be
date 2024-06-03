package com.example.douyin_chat.distributed;

import com.example.douyin_chat.constants.ServerConstants;
import com.example.douyin_chat.entity.ImNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author : zxm
 * @date: 2024/6/2 - 19:48
 * @Description: com.example.douyin_chat.distributed
 * @version: 1.0
 */
@Data
@Slf4j
public class WorkerRouter {
    // Zk客户端
    private CuratorFramework client = null;
    private String pathRegistered = null;
    private ImNode node = null;

    private static WorkerRouter singleInstance = null;
    private static final String path = ServerConstants.MANAGE_PATH;

    private ConcurrentHashMap<Long, PeerSender> workerMap = new ConcurrentHashMap<>();

    private BiConsumer<ImNode, PeerSender> runAfterAdd = (node, relaySender) -> { // 实现了BiConsumer函数式接口中的accept方法
        doAfterAdd(node, relaySender);
    };


    private Consumer<ImNode> runAfterRemove = (node) ->{
        doAfterRemove(node);
    };


    private void doAfterAdd(ImNode node, PeerSender relaySender) {
        if(null != relaySender){
            // 关闭老的连接
            relaySender.stopConnecting();
        }
    }




}
