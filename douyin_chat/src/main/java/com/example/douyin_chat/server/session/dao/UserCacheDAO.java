package com.example.douyin_chat.server.session.dao;

/**
 * @author : zxm
 * @date: 2024/5/31 - 21:26
 * @Description: com.example.douyin_chat.server.session.dao
 * @version: 1.0
 */
public interface UserCacheDAO {

    // 保持用户缓存
    void save(SessionCache s);

}
