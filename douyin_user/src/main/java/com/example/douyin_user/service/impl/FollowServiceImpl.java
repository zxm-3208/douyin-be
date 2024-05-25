package com.example.douyin_user.service.impl;

import com.example.douyin_commons.constant.RedisConstants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_commons.core.domain.UserListDTO;
import com.example.douyin_user.domain.po.dbAuth.DyFollow;
import com.example.douyin_user.domain.po.dbAuth.DyUser;
import com.example.douyin_user.domain.po.dbAuth.DyUserLikeMedia;
import com.example.douyin_user.domain.vo.AuthorFollowVo;
import com.example.douyin_user.mapper.master.DyFollowMapper;
import com.example.douyin_user.mapper.master.DyUserMapper;
import com.example.douyin_user.service.FollowService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * @author : zxm
 * @date: 2024/5/12 - 13:06
 * @Description: com.example.douyin_user.service.impl
 * @version: 1.0
 */
@Service
@Slf4j
public class FollowServiceImpl implements FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DyFollowMapper dyFollowMapper;

    @Autowired
    private DyUserMapper dyUserMapper;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket.icon}")
    private String bucket_icon_files;

    // TODO: 把用户点赞Redis删了，如果再去点赞，不会自动获取最新的点赞列表 （在点赞之前做一个判断，当前是没有点赞过还是Redis库丢失了）

    @Override
    public BaseResponse authorFollow(AuthorFollowVo authorFollowVo) {
        String userId = authorFollowVo.getUserId();     // 真实用户
        String authorId = authorFollowVo.getAuthorId();     // 目标用户
        String isFollow = authorFollowVo.getIsFollow();
        String key = RedisConstants.USER_FOLLOW_LIST_KEY + userId;
        String fansKey = RedisConstants.USER_FANS_LIST_KEY + authorId;
        String followListKey = RedisConstants.USER_FOLLOW_INFO_LIST_KEY + userId;
        String fansListKey = RedisConstants.USER_FANS_INFO_LIST_KEY + authorId;
        String realUserFansListKey = RedisConstants.USER_FANS_INFO_LIST_KEY + userId;
        if(isFollow.equals("-1")|| userId.equals(authorId)){
            return BaseResponse.fail("不可以关注自己");
        }

        // 执行follow动作
        long time = System.currentTimeMillis();
        log.info("isFollow:{}",isFollow);
        if("0".equals(isFollow)){
            Integer isSuccess = dyFollowMapper.addFollow(userId, authorId, new Timestamp(time));
            log.info("用户关注是否成功：{}", isSuccess);
            DyUser user = dyUserMapper.getEdit(authorId);
            log.info("用户关注列表更新：{}", user);
        }else{
            Integer isSuccess = dyFollowMapper.delFollow(userId, authorId);
            log.info("用户取关是否成功：{}", isSuccess);
            DyUser user = dyUserMapper.getEdit(authorId);
            log.info("用户关注列表更新：{}", user);
        }
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, System.currentTimeMillis());
        redisTemplate.opsForZSet().removeRangeByScore(fansKey, 0, System.currentTimeMillis());
        redisTemplate.opsForZSet().removeRangeByScore(followListKey, 0, System.currentTimeMillis());
        redisTemplate.opsForZSet().removeRangeByScore(fansListKey, 0, System.currentTimeMillis());
        redisTemplate.opsForZSet().removeRangeByScore(realUserFansListKey, 0, System.currentTimeMillis());
        return BaseResponse.success();
    }

    @Override
    public BaseResponse isFollow(AuthorFollowVo authorFollowVo) {
        String userId = authorFollowVo.getUserId();
        String authorId = authorFollowVo.getAuthorId();
        if(userId.equals(authorId)){
            return BaseResponse.success("-1");
        }
        String key = RedisConstants.USER_FOLLOW_LIST_KEY + userId;
        Double score = redisTemplate.opsForZSet().score(key, authorId);
        if (score == null) {
            DyFollow follow = dyFollowMapper.getFollowByUserIdAndAuthorId(userId, authorId);
            if(follow != null){
                redisTemplate.opsForZSet().add(key, authorId, follow.getFollowCreateTime().getTime());
                return BaseResponse.success("1");
            }
            else{
                return BaseResponse.success("0");
            }
        }
        return BaseResponse.success("1");
    }

    @Override
    public BaseResponse getFollowCount(String userId) {
        String key = RedisConstants.USER_FOLLOW_LIST_KEY + userId;
        Long size = redisTemplate.opsForZSet().size(key);
        if(size.equals(0L)) {
            List<DyFollow> followByUserIdList = dyFollowMapper.getFollowByUserId(userId);
            for(DyFollow x: followByUserIdList) {
                redisTemplate.opsForZSet().add(key, x.getFollowerId(), x.getFollowCreateTime().getTime());
            }
            size = redisTemplate.opsForZSet().size(key);
        }
        return BaseResponse.success(size);
    }

    @Override
    public BaseResponse getFansCount(String userId) {
        String key = RedisConstants.USER_FANS_LIST_KEY + userId;
        Long size = redisTemplate.opsForZSet().size(key);
        if(size.equals(0L)) {
            List<DyFollow> fansByAuthorIdList = dyFollowMapper.getFansByAuthorId(userId);
            for(DyFollow x: fansByAuthorIdList) {
                redisTemplate.opsForZSet().add(key, x.getUserId(), x.getFollowCreateTime().getTime());
            }
            size = redisTemplate.opsForZSet().size(key);
        }
        return BaseResponse.success(size);
    }

}
