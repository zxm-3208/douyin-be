package com.example.douyin_chat.server.session.dao;

import com.example.douyin_chat.server.session.entity.SessionCache;
import com.example.douyin_chat.server.session.entity.UserCache;

/**
 * @author : zxm
 * @date: 2024/5/31 - 21:26
 * @Description: com.example.douyin_chat.server.session.dao
 * @version: 1.0
 */
public interface UserCacheDAO {

    // 保持用户缓存
    void save(UserCache s);

    // 获取用户缓存
    UserCache get(String userId);

    // 增加用户的会话
    void addSession(String uid, SessionCache session);

    // 删除用户的会话
    void removeSession(String uid, String sessionId);


}
