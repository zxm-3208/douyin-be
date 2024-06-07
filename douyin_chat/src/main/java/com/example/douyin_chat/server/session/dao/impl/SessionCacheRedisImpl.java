package com.example.douyin_chat.server.session.dao.impl;

import com.example.douyin_chat.server.session.dao.SessionCacheDAO;
import com.example.douyin_chat.server.session.entity.SessionCache;
import com.example.douyin_chat.util.JsonUtil;
import com.example.douyin_commons.constant.RedisConstants;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * @author : zxm
 * @date: 2024/5/31 - 21:26
 * @Description: com.example.douyin_chat.server.session.dao.impl
 * @version: 1.0
 */
@Repository
public class SessionCacheRedisImpl implements SessionCacheDAO {

    @Autowired
    protected StringRedisTemplate redisTemplate;

    @Override
    public void save(SessionCache s) {
        String key = RedisConstants.SESSIONCACHE_ID_KEY + s.getSessionId();
        String value = JsonUtil.pojoToJson(s);
        redisTemplate.opsForValue().set(key, value, RedisConstants.SESSIONCACHE_ID_TTL, TimeUnit.HOURS);
    }

    @Override
    public SessionCache get(String sessionId) {
        String key = RedisConstants.SESSIONCACHE_ID_KEY + sessionId;
        String value = (String) redisTemplate.opsForValue().get(key);
        if(!StringUtils.isEmpty(value)){
            return JsonUtil.jsonToPojo(value, SessionCache.class);
        }
        return null;
    }

    @Override
    public void remove(String sessionId) {
        String key = RedisConstants.SESSIONCACHE_ID_KEY + sessionId;
        redisTemplate.delete(key);
    }
}
