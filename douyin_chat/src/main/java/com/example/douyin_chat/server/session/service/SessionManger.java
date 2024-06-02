package com.example.douyin_chat.server.session.service;

import com.example.douyin_chat.entity.ImNode;
import com.example.douyin_chat.server.session.LocalSession;
import com.example.douyin_chat.server.session.RemoteSession;
import com.example.douyin_chat.server.session.ServerSession;
import com.example.douyin_chat.server.session.dao.SessionCacheDAO;
import com.example.douyin_chat.server.session.dao.UserCacheDAO;
import com.example.douyin_chat.server.session.entity.SessionCache;
import com.example.douyin_chat.server.session.entity.UserCache;
import com.example.douyin_chat.util.JsonUtil;
import com.example.douyin_chat.util.Notification;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : zxm
 * @date: 2024/5/31 - 21:19
 * @Description: com.example.douyin_chat.server.session
 * @version: 1.0
 */
@Slf4j
@Data
@Repository
public class SessionManger {

    @Autowired
    UserCacheDAO userCacheDAO;

    @Autowired
    SessionCacheDAO sessionCacheDAO;

    // 饿汉式单例
    private static SessionManger singleInstance = new SessionManger();

    public static SessionManger inst(){
        return singleInstance;
    }

    // 会话清单： 含本地会话、远程会话
    private ConcurrentHashMap<String, ServerSession> sessionMap = new ConcurrentHashMap();

    /**
     * 登录成功之后，增加 sessin 对象
     */
    public void addLocalSession(LocalSession session){
        // 1. 保存本地的session到会话清单
        String sessionId = session.getSessionId();
        sessionMap.put(sessionId, session);
        String uid = session.getUser().getUserId();

        // 2. 缓存session到Redis
        ImNode node = ImWorker.getInst().getLocalNodeInfo();
        SessionCache sessionCache = new SessionCache(sessionId, uid, node);
        sessionCacheDAO.save(sessionCache);     // key：sessionId, value: sessionCache pojo

        // 3. 增加用户的session信息到用户Redis缓存
        userCacheDAO.addSession(uid, sessionCache); // key：userId, value: sessionCache pojo

        // 4. 增加用户数
        OnlineCounter.getInst().increment();
        log.info("本地session增加:{}， 在线总数:{}", JsonUtil.pojoToJson(session.getUser()), OnlineCounter.getInst().getCurValue());
        ImWorker.getInst().incBalance();

        // 5. 通知其他节点，有会话上线
        notifyOtherImNodeOnLine(session);
    }

    /**
     * 根据用户ID，获取session对象
     */
    public List<ServerSession> getSessionsBy(String userId){
        UserCache user = userCacheDAO.get(userId);
        if(null == user){
            log.info("用户:{}下线了？没有在缓存中找到记录", userId);
            return null;
        }
        Map<String, SessionCache> allSession = user.getMap();
        if(null == allSession || allSession.size()==0){
            log.info("用户:{}下线了？没有任何会话", userId);
        }
        List<ServerSession> sessions = new LinkedList<>();
        allSession.values().stream().forEach(sessionCache -> {
            String sid = sessionCache.getSessionId();
            // 在本地，从会话列表中获取
            ServerSession session = sessionMap.get(sid);
            // 没有命中，则创建远程的session，加入会话集合
            if(session==null){
                session = new RemoteSession(sessionCache);
                sessionMap.put(sid, session);
            }
            sessions.add(session);
        });
        return sessions;
    }

    /**
     * 关闭连接
     */
    public void closeSession(ChannelHandlerContext ctx){
        LocalSession session = ctx.channel().attr(LocalSession.SESSION_KEY).get();

        if(null == session || !session.isValid()){
            log.error("session is null or is Valid");
            return;
        }

        session.close();
        // 删除本地的会话和远程会话
        removeSession(session.getSessionId());

        // 通知其他节点，用户下线
        notifyOtherImNodeOffLine(session);
    }

    /**
     * 通知其他节点，有会话下线
     */
    private void notifyOtherImNodeOffLine(LocalSession session){
        if(null==session || session.isValid()){
            log.error("session is null or isValid");
            return;
        }
        // 下线的通知
        int type = Notification.SESSION_OFF;
        Notification<Notification.ContentWrapper> notification = Notification.wrapContent(session.getSessionId());
        notification.setType(type);
        WorkerRouter.getInst().sendNotification(JsonUtil.pojoToJson(notification));
    }

    /**
     * 通知其他节点有会话上线
     */
    private void notifyOtherImNodeOnLine(LocalSession session){
        // 上线的通知
        int type = Notification.SESSION_ON;
        Notification<Notification.ContentWrapper> notification = Notification.wrapContent(session.getSessionId());
        notification.setType(type);
        WorkerRouter.getInst().sendNotification(JsonUtil.pojoToJson(notification));
    }

    /**
     * 删除session
     */
    public void removeSession(String sessionId){
        // 会话列表中没有该session
        if(!sessionMap.containsKey(sessionId))
            return;
        ServerSession session = sessionMap.get(sessionId);
        String uid = session.getUserId();
        // 减少用户数
        OnlineCounter.getInst().decrement();
        log.info("本地session减少:{}下线了， 在线总数:{}", uid, OnlineCounter.getInst.getCurValue());
        ImWorker.getInst().decrBalance();

        // 分布式: 分布式保存user和所有session
        // 根据sessionId删除用户的会话
        userCacheDAO.removeSession(uid, sessionId);
        // 删除缓存session
        sessionCacheDAO.remove(sessionId);

        // 本地：从会话集合中，删除会话
        sessionMap.remove(sessionId);
    }

    /**
     * 远程用户下线，数据堆积，删除session
     */
    public void removeRemoteSession(String sessionId){
        if(!sessionMap.containsKey(sessionId)){
            return;
        }
        sessionMap.remove(sessionId);
    }

}
