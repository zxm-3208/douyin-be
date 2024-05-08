package com.example.douyin_user.service.impl;

import com.example.douyin_commons.constant.RedisConstants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_commons.utils.UserHolder;
import com.example.douyin_user.mapper.second.MediaFilesMapper;
import com.example.douyin_user.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author : zxm
 * @date: 2024/5/6 - 21:38
 * @Description: com.example.douyin_user.service.impl
 * @version: 1.0
 */
@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Override
    public BaseResponse addLike(String mediaId) {
        String userId = UserHolder.getUser().getId();
        String key = RedisConstants.MEDIA_USER_LIKE_KEY + mediaId;
        // 首先判断用户是否已经点过赞
        Double score = redisTemplate.opsForZSet().score(key, userId);
        if (score == null) {
            // 没有，则添加
            int isSuccess = mediaFilesMapper.addMediaLikeById(mediaId);
            if (isSuccess > 0) {
                redisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        }else {
            // 有则删除
            int isSuccess = mediaFilesMapper.delMediaLikeById(mediaId);
            if (isSuccess > 0) {
                redisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
        return null;
    }
}
