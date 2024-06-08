package com.example.douyin_chat_server.session.dao;


import com.example.douyin_chat_server.session.entity.SessionCache;

/**
 * @author : zxm
 * @date: 2024/5/31 - 21:25
 * @Description: com.example.douyin_chat.server.session.dao
 * @version: 1.0
 */
public interface SessionCacheDAO {
    // 保存会话到缓存
    void save(SessionCache s);

    // 从缓存获取会话
    SessionCache get(String sessionId);

    // 删除会话
    void remove(String sessionId);


}
