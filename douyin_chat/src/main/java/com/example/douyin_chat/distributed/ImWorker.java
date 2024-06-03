package com.example.douyin_chat.distributed;

import com.example.douyin_chat.constants.ServerConstants;
import com.example.douyin_chat.distributed.zookeeper.CuratorZKclient;
import com.example.douyin_chat.entity.ImNode;
import com.example.douyin_chat.util.JsonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * @author : zxm
 * @date: 2024/6/2 - 19:48
 * @Description: IM 节点的ZK协调客户端（与ImNode一对一绑定）
 * @version: 1.0
 */
@Data
@Slf4j
public class ImWorker {
    // ZK curator客户端
    private CuratorFramework client = null;

    // 保存当前Znode节点的路径，创建后返回
    private String pathRegistered = null;

    private ImNode localNode = null;

    private static ImWorker singleInstance = null;

    private boolean inited = false;

    // 取得单例，保证一个应用只有该类的一个实例(与ImNode一对一绑定)
    public synchronized static ImWorker getInst(){
        if(null == singleInstance){
            singleInstance = new ImWorker();
            singleInstance.localNode = new ImNode();
        }
        return singleInstance;
    }

    private ImWorker(){

    }

    // 在zookeeper中创建临时节点(当前worker实例)
    public synchronized void init(){
        if(inited){
            return;
        }
        inited = true;
        if(null == client){
            this.client = CuratorZKclient.instance.getClient();
        }
        if(null == localNode){
            localNode = new ImNode();
        }
        // 没有子节点就删除,就删除该工作节点的父节点
        deleteWhenHasNoChildren(ServerConstants.MANAGE_PATH);
        // 为null就创建该工作节点的父节点
        createdParentIfNeeded(ServerConstants.MANAGE_PATH);

        // 创建一个 ZNode 节点
        // 节点的payload 为当前worker实例
        try{
            byte[] payload = JsonUtil.object2JsonBytes(localNode);
            pathRegistered = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(ServerConstants.PATH_PREFIX, payload);
            // 为node设置id
            localNode.setId(getId());
            log.info("本地节点， path={}, id={}", pathRegistered, localNode.getId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void setLocalNode(String ip, int port){
        localNode.setHost(ip);
        localNode.setPort(port);
    }

    /**
     * 取得IM节点编号
     */
    public long getId(){
        return getIdByPath(pathRegistered);
    }

    /**
     * 取得IM节点编号
     */
    private long getIdByPath(String path) {
        String sid = null;
        if(null == path){
            throw new RuntimeException("节点路径有误");
        }
        int index = path.lastIndexOf(ServerConstants.PATH_PREFIX);
        if(index >= 0){
            index += ServerConstants.PATH_PREFIX.length();    // index是首字母的索引，所以需要加上长度
            // index 之后的是id，截取Id
            sid = index <= path.length() ? path.substring(index): null;
        }
        if(null == sid){
            throw new RuntimeException("节点ID获取失败");
        }
        return Long.parseLong(sid);
    }

    /**
     * 创建父节点
     */
    private void createdParentIfNeeded(String managePath){
        try{
            Stat stat = client.checkExists().forPath(managePath);
            if(null == stat){
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(managePath);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void deleteWhenHasNoChildren(String path){

        int index = path.lastIndexOf("/");

        String parent = path.substring(0, index);
        boolean exist = isNodeExist(parent);
        if(exist){
            List<String> children = getChildren(parent);
            if(null!=children && children.size() == 0){
                delPath(parent);
                log.info("删除空的父节点：{}", parent);
            }
        }
    }

    /**
     * 检查节点
     */
    public boolean isNodeExist(String zkPath){
        try{
            Stat stat = client.checkExists().forPath(zkPath);
            if(null != stat){
                log.info("节点不存在：{}", zkPath);
                return false;
            }else{
                log.info("节点存在 stat is:{}", stat.toString());
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取子节点
     */
    public List<String> getChildren(String path){
        // 检测是否存在该路径
        try{
            List<String> children = client.getChildren().forPath(path);
            return children;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除该路径
     */
    public boolean delPath(String path){
        boolean b = false;
        // 检测是否存在该路径
        try{
            Void stat = client.delete().forPath(path);      // 不可实例化的占位符
            b = stat == null? false:true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return b;
    }

    /**
     * 有用户成功登录，增加负载
     */
    public boolean incBalance(){
        if(null == localNode){
            throw new RuntimeException("还没有设置Node 节点");
        }
        // 增加负载，并回写到zookeeper
        while(true){
            try{
                localNode.incrementBalance();
                byte[] payload = JsonUtil.object2JsonBytes(localNode);
                client.setData().forPath(pathRegistered, payload);      // TODO: 不是线程安全的，要加版本号
                return true;
            }catch (Exception e){
                return false;
            }
        }
    }

    /**
     * 有用户下线，减少负载
     */
    public boolean decrBalance(){
        if(null == localNode){
            throw new RuntimeException("还没有设置Node节点");
        }
        while(true){
            try{
                localNode.decrementBalance();
                byte[] payload = JsonUtil.object2JsonBytes(localNode);
                client.setData().forPath(pathRegistered, payload);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    static{
        // JVM 关闭时的钩子函数
        Runtime.getRuntime().addShutdownHook(
                new Thread(()->{
                    ImWorker.getInst().deleteNode();
                }, " 关掉worker, 删除zkz节点")
        );
    }

    private void deleteNode(){
        log.info("删除worker node, path={}, id={}", pathRegistered, localNode.getId());
        try{
            Stat stat = client.checkExists().forPath(pathRegistered);
            if(null == stat){
                log.info("节点不存在：{}", pathRegistered);
            }
            else{
                client.delete().forPath(pathRegistered);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
