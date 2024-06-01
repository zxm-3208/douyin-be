package com.example.douyin_chat.server.session.dao.impl;

import com.example.douyin_chat.server.session.dao.UserCacheDAO;
import com.example.douyin_chat.server.session.entity.SessionCache;
import com.example.douyin_chat.server.session.entity.UserCache;
import com.example.douyin_chat.util.JsonUtil;
import com.example.douyin_commons.constant.RedisConstants;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author : zxm
 * @date: 2024/5/31 - 21:26
 * @Description: com.example.douyin_chat.server.session.dao.impl
 * @version: 1.0
 */
public class UserCacheRedisImpl implements UserCacheDAO {

    @Autowired
    protected StringRedisTemplate redisTemplate;


    // 更新Redis中用户对话的session
    @Override
    public void save(UserCache s) {
        String key = RedisConstants.USERCACHE_UID_KEY + s.getUserId();
        String value = JsonUtil.pojoToJson(s);
        redisTemplate.opsForValue().set(key, value, RedisConstants.USERCACHE_UID_TTL, TimeUnit.HOURS);
    }

    // 获取用户的对话session
    @Override
    public UserCache get(String userId) {
        String key = RedisConstants.USERCACHE_UID_KEY + userId;
        String value = (String) redisTemplate.opsForValue().get(key);
        if(!StringUtils.isEmpty(value)){
            return JsonUtil.jsonToPojo(value, UserCache.class);
        }
        return null;
    }

    // 新增用户的对话session
    @Override
    public void addSession(String uid, SessionCache session) {
        UserCache user = get(uid);
        if(null==user){
            user = new UserCache(uid);
        }
        user.addSession(session);
        save(user);
    }

    // 删除用户的对话session
    @Override
    public void removeSession(String uid, String sessionId) {
        UserCache user = get(uid);
        if(null==user){
            user = new UserCache(uid);
        }
        user.removeSession(sessionId);
        save(user);
    }
}
