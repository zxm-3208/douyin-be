package com.example.douyin_chat_server.distributed;


import com.example.douyin_chat_commons.constants.ServerConstants;
import com.example.douyin_chat_commons.distributed.CuratorZKclient;
import com.example.douyin_chat_commons.domain.po.ImNode;
import com.example.douyin_chat_commons.protocol.bean.ProtoMsgOuterClass;
import com.example.douyin_chat_server.protoBuilder.NotificationMsgBuilder;
import com.example.douyin_chat_commons.util.JsonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author : zxm
 * @date: 2024/6/2 - 19:48
 * @Description: 管理远程节点
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

    // nodeId ,PeerSender
    // 远程节点
    private ConcurrentHashMap<Long, PeerSender> workerMap = new ConcurrentHashMap<>();

    private boolean inited = false;

    public synchronized static WorkerRouter getInstance(){
        if(null == singleInstance){
            singleInstance = new WorkerRouter();
        }
        return singleInstance;
    }

    private WorkerRouter(){}

    private BiConsumer<ImNode, PeerSender> runAfterAdd = (node, relaySender) -> { // 实现了BiConsumer函数式接口中的accept方法
        doAfterAdd(node, relaySender);
    };

    private Consumer<ImNode> runAfterRemove = (node) ->{
        doAfterRemove(node);
    };

    /**
     * 初始化节点管理
     */
    public void init(){
        if(inited){
            return;
        }
        inited = true;

        try{
            if(null ==  client){
                this.client = CuratorZKclient.instance.getClient();
            }

            // 订阅节点的增加和删除事件 (缓存监听，可以监听Node(自身), path(子节点), tree(全部))
            PathChildrenCache childrenCache = new PathChildrenCache(client, path, true);
            PathChildrenCacheListener childrenCacheListener = new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    log.info("开始监听其他的ImWorker子节点");
                    ChildData data = event.getData();
                    switch(event.getType()){    // 节点的增加和删除会改变连接方式
                        case CHILD_ADDED:
                            log.info("CHILD_ADDED:{} 数据:{}", data.getPath(), data.getData());
                            processNodeAdded(data);
                        case CHILD_REMOVED:
                            log.info("CHILD_RFEMOVED:{} 数据:{}", data.getPath(), data.getData());
                            processNodeRemoved(data);
                        case CHILD_UPDATED:
                            log.info("CHILD_UPDATE:{} 数据:{}", data.getPath(), data.getData());
                        default:
                            log.debug("[PathChildrenCache] 节点数据为空, path={}", data == null? "null":data.getPath());
                            break;
                    }
                }
            };
            childrenCache.getListenable().addListener(childrenCacheListener);
            System.out.println("Register zk watcher successfully!");
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT); //在所有的初始化事件（如现有的子节点触发的 CHILD_ADDED 事件）都被处理完毕后，才会开始监听新的变更事件。

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 节点增加的处理
     */
    private void processNodeAdded(ChildData data){
        byte[] payload = data.getData();
        ImNode node = JsonUtil.jsonBytes2Object(payload, ImNode.class);

        long id = ImWorker.getInst().getIdByPath(data.getPath());
        node.setId(id);

        log.info("[TreeCache]节点更新端口, path={}, data={}", data.getPath(), JsonUtil.pojoToJson(node));
        if(node.equals(getLocalNode())){
            log.info("[TreeCache]本地节点, path={}, data={}", data.getPath(), JsonUtil.pojoToJson(node));
            return;
        }

        // 判断workerMap中否出现过该id
        PeerSender relaySender = workerMap.get(node.getId());
        // 重复收到注册的事件
        if(null != relaySender && relaySender.getRmNode().equals(node)){
            log.info("[TreeCache]节点重复增加, path={}, data={}", data.getPath(), JsonUtil.pojoToJson(node));
            return;
        }

        if(runAfterAdd != null){
            runAfterAdd.accept(node, relaySender);
        }
    }

    /**
     * 节点删除的处理
     */
    private void processNodeRemoved(ChildData data){
        byte[] payload = data.getData();
        ImNode node = JsonUtil.jsonBytes2Object(payload, ImNode.class);

        long id = ImWorker.getInst().getIdByPath(data.getPath());
        node.setId(id);
        log.info("[TreeCache]节点删除, path={}, data={}", data.getPath(), JsonUtil.pojoToJson(node));

        if(runAfterRemove != null){
            runAfterRemove.accept(node);
        }
    }

    public ImNode getLocalNode(){
        return ImWorker.getInst().getLocalNode();
    }

    private void doAfterAdd(ImNode node, PeerSender relaySender) {
        if(null != relaySender){
            // 关闭老的连接 (健壮性)
            relaySender.stopConnecting();
        }
        // 创建一个消息转发器
        relaySender = new PeerSender(node);
        // 建立转发的连接
        relaySender.doConnect();

        workerMap.put(node.getId(), relaySender);
    }

    private void doAfterRemove(ImNode node){
        PeerSender peerSender = workerMap.get(node.getId());
        if(null != peerSender){
            peerSender.stopConnecting();
            workerMap.remove(node.getId());
        }
    }

    public PeerSender route(long nodeId){
        PeerSender peerSender = workerMap.get(nodeId);
        if(null!=peerSender){
            return peerSender;
        }
        return null;
    }

    public void sendNotification(String json){
        workerMap.keySet().stream().forEach(
                key ->{
                    if(!key.equals(getLocalNode().getId())){
                        PeerSender peerSender = workerMap.get(key);
                        ProtoMsgOuterClass.ProtoMsg.Message pkg = NotificationMsgBuilder.buildNotification(json);
                        peerSender.writeAndFlush(pkg);
                    }
                }
        );
    }

    public void remove(ImNode remoteNode){
        workerMap.remove(remoteNode.getId());
        log.info("[TreeCache]移除远程节点信息， node={}", JsonUtil.pojoToJson(remoteNode));
    }

}
