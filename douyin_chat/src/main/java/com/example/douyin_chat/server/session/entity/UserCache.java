package com.example.douyin_chat.server.session.entity;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author : zxm
 * @date: 2024/5/31 - 21:59
 * @Description: com.example.douyin_chat.server.session.dao.entity
 * @version: 1.0
 */
@Data
public class UserCache {
    private String userId;
    private Map<String, SessionCache> map = new LinkedHashMap<>(10);

    public UserCache(String userId){
        this.userId = userId;
    }

    // 为用户增加session
    public void addSession(SessionCache session){
        map.put(session.getSessionId(), session);
    }

    // 用用户移除session
    public void removeSession(String sessionId){
        map.remove(sessionId);
    }

}
