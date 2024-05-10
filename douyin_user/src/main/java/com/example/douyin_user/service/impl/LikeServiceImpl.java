package com.example.douyin_user.service.impl;

import com.example.douyin_commons.constant.RedisConstants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_commons.core.domain.CoverPublistDTO;
import com.example.douyin_commons.core.domain.MediaPublistDTO;
import com.example.douyin_commons.utils.UserHolder;
import com.example.douyin_user.domain.po.dbAuth.DyUserLikeMedia;
import com.example.douyin_user.domain.po.dbMedia.DyMedia;
import com.example.douyin_user.domain.po.dbMedia.DyPublish;
import com.example.douyin_user.domain.vo.VediaUserLikes;
import com.example.douyin_user.mapper.master.DyUserLikeMediaMapper;
import com.example.douyin_user.mapper.second.MediaFilesMapper;
import com.example.douyin_user.mapper.second.PublishMapper;
import com.example.douyin_user.service.LikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.Timestamp;

/**
 * @author : zxm
 * @date: 2024/5/6 - 21:38
 * @Description: com.example.douyin_user.service.impl
 * @version: 1.0
 */
@Service
@Slf4j
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MediaFilesMapper mediaFilesMapper;
    @Autowired
    private DyUserLikeMediaMapper dyUserLikeMediaMapper;
    @Autowired
    private PublishMapper publishMapper;

    @Override
    public BaseResponse addLike(VediaUserLikes vediaUserLikes) {
        String userId = vediaUserLikes.getUserId();
        String mediaId = vediaUserLikes.getMediaId();
        String mediaUserLikeKey = RedisConstants.MEDIA_USER_LIKE_KEY + mediaId;
        String userLikeMediaListKey = RedisConstants.USER_LIKE_MEDIA_LIST_KEY + userId;
        String userLikeMediaCoverListKey = RedisConstants.LIKE_USER_COVER_KEY + userId;
        String publistUserCoverKey = RedisConstants.PUBLIST_USER_COVER_KEY + userId;
        // 首先判断用户是否已经点过赞
        Double score = redisTemplate.opsForZSet().score(mediaUserLikeKey, userId);
        log.info("当前用户是否存在：{}", score);
        // 没有，则添加
        if (score == null) {
            // 更新点赞数量以及视频的用户点赞列表
            Integer isSuccess = mediaFilesMapper.addMediaLikeById(mediaId);
            log.info("视频的用户点赞列表添加是否成功：{}", isSuccess);
            if (isSuccess != 0) {
                redisTemplate.opsForZSet().add(mediaUserLikeKey, userId, System.currentTimeMillis());
            }
            // 更新用户点赞的视频列表
            isSuccess = dyUserLikeMediaMapper.addLikeMeida(userId, mediaId, new Timestamp(System.currentTimeMillis()));
            log.info("用户点赞的视频列表添加是否成功：{}", isSuccess);
            if (isSuccess != 0) {
                DyMedia dyMedia = mediaFilesMapper.getMediaUrlByMediaId(mediaId);
                if(dyMedia!=null) {
                    MediaPublistDTO mediaPublistDTO = new MediaPublistDTO(mediaId, dyMedia.getMediaUrl());
                    redisTemplate.opsForZSet().add(userLikeMediaListKey, mediaPublistDTO, System.currentTimeMillis());
                }
                // 更新用户点赞的视频列表封面
                DyPublish dPublish = publishMapper.getCoverUrlByMediaId(mediaId);
                if(dPublish!=null) {
                    CoverPublistDTO coverPublistDTO = new CoverPublistDTO(mediaId, dPublish.getImgUrl());
                    redisTemplate.opsForZSet().add(userLikeMediaCoverListKey, coverPublistDTO, System.currentTimeMillis());
                }
            }
        // 有则删除
        }else {
            // 更新点赞数量以及视频的用户点赞列表
            Integer isSuccess = mediaFilesMapper.delMediaLikeById(mediaId);
            log.info("视频的用户点赞列表删除是否成功：{}", isSuccess);
            if (isSuccess != 0) {
                redisTemplate.opsForZSet().remove(mediaUserLikeKey, userId.toString());
            }
            // 更新用户点赞的视频列表
            isSuccess = dyUserLikeMediaMapper.delLikeMeida(userId, mediaId);
            log.info("用户点赞的视频列表删除是否成功：{}", isSuccess);
            if (isSuccess != 0) {
                DyMedia dyMedia = mediaFilesMapper.getMediaUrlByMediaId(mediaId);
                if(dyMedia!=null) {
                    MediaPublistDTO mediaPublistDTO = new MediaPublistDTO(mediaId, dyMedia.getMediaUrl());
                    redisTemplate.opsForZSet().remove(userLikeMediaListKey, mediaPublistDTO, System.currentTimeMillis());
                }
                // 更新用户点赞的视频列表封面
                DyPublish dPublish = publishMapper.getCoverUrlByMediaId(mediaId);
                if(dPublish!=null) {
                    CoverPublistDTO coverPublistDTO = new CoverPublistDTO(mediaId, dPublish.getImgUrl());
                    redisTemplate.opsForZSet().remove(userLikeMediaCoverListKey, coverPublistDTO);
                }
            }
        }
        return null;
    }

    @Override
    public BaseResponse getLikeCount(VediaUserLikes vediaUserLikes) {
        String mediaId = vediaUserLikes.getMediaId();
        String key = RedisConstants.MEDIA_USER_LIKE_KEY + mediaId;
        Long size = redisTemplate.opsForZSet().size(key);
        log.info("视频:{}的点赞数量：{}",mediaId, size);
        return BaseResponse.success(size);
    }

    @Override
    public BaseResponse initLikeFlag(VediaUserLikes vediaUserLikes) {
        String mediaId = vediaUserLikes.getMediaId();
        String key = RedisConstants.MEDIA_USER_LIKE_KEY + mediaId;
        String userId = vediaUserLikes.getUserId();
        Double score = redisTemplate.opsForZSet().score(key, userId);
        log.info("score:{}", score);
        if(score==null){
            return BaseResponse.success(-1);
        }
        else{
            return BaseResponse.success(1);
        }
    }

    @Override
    public BaseResponse getUserLikeList(VediaUserLikes vediaUserLikes) {
        return null;
    }
}
