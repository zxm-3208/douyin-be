package com.example.douyin_publish.utils;

import com.example.douyin_commons.core.domain.CoverPublistDTO;
import com.example.douyin_commons.core.domain.MediaPublistDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

/**
 * @author : zxm
 * @date: 2024/4/26 - 14:16
 * @Description: com.example.douyin_publish.utils
 * @version: 1.0
 */
@Component
public class ZSetUtils {
    @Autowired
    private RedisTemplate redisTemplate;

    public void addObjectToZSet(String key, CoverPublistDTO obj, double score) {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        zSetOps.add(key, obj, score);
    }

    public void addObjectToZSet(String key, MediaPublistDTO obj, double score) {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        zSetOps.add(key, obj, score);
    }

}
