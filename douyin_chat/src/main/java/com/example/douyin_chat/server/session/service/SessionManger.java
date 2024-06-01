package com.example.douyin_chat.server.session.service;

import com.example.douyin_chat.entity.ImNode;
import com.example.douyin_chat.server.session.LocalSession;
import com.example.douyin_chat.server.session.ServerSession;
import com.example.douyin_chat.server.session.dao.SessionCacheDAO;
import com.example.douyin_chat.server.session.dao.UserCacheDAO;
import com.example.douyin_chat.server.session.entity.SessionCache;
import com.example.douyin_chat.util.JsonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    public List<ServerSession> getSessionsBy(String userId){

    }


}
