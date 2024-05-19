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
        // follow前需要判断Redis是否为空，如果为空需要从mysql获取数据
        Long size = redisTemplate.opsForZSet().size(key);
        if(size.equals(0L)) {
            List<DyFollow> followByUserIdList = dyFollowMapper.getFollowByUserId(userId);
            for(DyFollow x: followByUserIdList) {
               redisTemplate.opsForZSet().add(key, x.getFollowerId(), x.getFollowCreateTime().getTime());
            }
        }
        size = redisTemplate.opsForZSet().size(fansKey);
        if(size.equals(0L)) {
            List<DyFollow> followByUserIdList = dyFollowMapper.getFollowByUserId(authorId);
            for(DyFollow x: followByUserIdList) {
                redisTemplate.opsForZSet().add(fansKey, x.getUserId(), x.getFollowCreateTime().getTime());
            }
        }
        size = redisTemplate.opsForZSet().size(followListKey);
        if(size.equals(0L)) {
            List<DyFollow> followInfoByUserAndFollowList = dyFollowMapper.getFollowInfoByUserAndFollow(userId);
            for(DyFollow x: followInfoByUserAndFollowList) {
                UserListDTO userListDTO = new UserListDTO(x.getFollowerId(), x.getDyUser().getIcon(), x.getDyUser().getUserName(), x.getDyUser().getIntroduction());
                redisTemplate.opsForZSet().add(followListKey, userListDTO, x.getFollowCreateTime().getTime());
            }
        }
        size = redisTemplate.opsForZSet().size(fansListKey);
        if(size.equals(0L)) {
            List<DyFollow> fansInfoByUserAndFollowList = dyFollowMapper.getFansInfoByUserAndFollow(authorId);
            for(DyFollow x: fansInfoByUserAndFollowList) {
                UserListDTO userListDTO = new UserListDTO(x.getUserId(), x.getDyUser().getIcon(), x.getDyUser().getUserName(), x.getDyUser().getIntroduction());
                redisTemplate.opsForZSet().add(fansListKey, userListDTO, x.getFollowCreateTime().getTime());
            }
        }

        // 执行follow动作
        long time = System.currentTimeMillis();
        log.info("isFollow:{}",isFollow);
        if("0".equals(isFollow)){
            Integer isSuccess = dyFollowMapper.addFollow(userId, authorId, new Timestamp(time));
            log.info("用户关注是否成功：{}", isSuccess);
            if (isSuccess != 0) {
                redisTemplate.opsForZSet().add(key, authorId, time);
                redisTemplate.opsForZSet().add(fansKey, userId, time);
            }
            DyUser user = dyUserMapper.getEdit(authorId);
            log.info("用户关注列表更新：{}", user);
            if(user != null) {
                // Redis关注者列表
                // 更新真实用户Redis关注者列表
                UserListDTO userListDTO = new UserListDTO(user.getId(), user.getIcon(), user.getUserName(), user.getIntroduction());
                redisTemplate.opsForZSet().add(followListKey, userListDTO, time);
                // Redis粉丝列表
                DyUser realUser = dyUserMapper.getEdit(userId);
                if(user != null) {
                    // 更新目标用户Redis粉丝列表 (目标用户粉丝列表增加真实用户信息)
                    // 先要判断目标用户Redis粉丝列表是否为空，为空需要先从Mysql中获取数据
                    size = redisTemplate.opsForZSet().size(fansListKey);
                    if(size==0){
                        List<DyFollow> dyFollowList = dyFollowMapper.getFansInfoByUserAndFollow(authorId);
                        if(dyFollowList.size()>0){
                            for(DyFollow x: dyFollowList) {
                                userListDTO = new UserListDTO(x.getUserId(), x.getDyUser().getIcon(), x.getDyUser().getUserName(), x.getDyUser().getIntroduction());
                                redisTemplate.opsForZSet().add(fansListKey, userListDTO, x.getFollowCreateTime().getTime());
                            }
                        }
                    }
                    userListDTO = new UserListDTO(realUser.getId(), realUser.getIcon(), realUser.getUserName(), realUser.getIntroduction());
                    redisTemplate.opsForZSet().add(fansListKey, userListDTO, time);
                }
            }
        }else{
            Integer isSuccess = dyFollowMapper.delFollow(userId, authorId);
            log.info("用户取关是否成功：{}", isSuccess);
            if(isSuccess != 0){
                redisTemplate.opsForZSet().remove(key, authorId);
                redisTemplate.opsForZSet().remove(fansKey, userId);
            }
            DyUser user = dyUserMapper.getEdit(authorId);
            log.info("用户关注列表更新：{}", user);
            if(user != null) {
                // Redis关注者列表
                UserListDTO userListDTO = new UserListDTO(user.getId(), user.getIcon(), user.getUserName(), user.getIntroduction());
                redisTemplate.opsForZSet().remove(followListKey, userListDTO);
                // Redis粉丝列表
                DyUser realUser = dyUserMapper.getEdit(userId);
                if(user != null) {
                    // 更新真实用户Redis粉丝列表
                    userListDTO = new UserListDTO(realUser.getId(), realUser.getIcon(), realUser.getUserName(), realUser.getIntroduction());
                    redisTemplate.opsForZSet().remove(fansListKey, userListDTO);
                }
            }
        }
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
