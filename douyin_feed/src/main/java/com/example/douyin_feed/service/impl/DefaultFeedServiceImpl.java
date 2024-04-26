package com.example.douyin_feed.service.impl;

import com.example.douyin_commons.constant.RedisConstants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_feed.domain.dto.MediaPublistDTO;
import com.example.douyin_feed.domain.po.DyMedia;
import com.example.douyin_feed.domain.po.DyPublish;
import com.example.douyin_feed.domain.po.MediaJoinPublish;
import com.example.douyin_feed.domain.vo.ClickPlayVo;
import com.example.douyin_feed.domain.vo.MediaPlayVo;
import com.example.douyin_feed.domain.vo.UrlListVo;
import com.example.douyin_feed.mapper.MediaFilesMapper;
import com.example.douyin_feed.mapper.PublishMapper;
import com.example.douyin_feed.service.DefaultFeedService;
import com.example.douyin_feed.utils.ZSetUtils;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author : zxm
 * @date: 2024/4/24 - 16:24
 * @Description: 按照发布时间排序
 * @version: 1.0
 */
@Service
@Slf4j
public class DefaultFeedServiceImpl implements DefaultFeedService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MediaFilesMapper mediaFilesMapper;
    @Autowired
    private PublishMapper publishMapper;
    @Autowired
    private ZSetUtils zSetUtils;
    // 视频文件桶
    @Value("${minio.bucket.videofiles}")
    private String bucket_videofiles;
    // 普通文件桶
    @Value("${minio.bucket.files}")
    private String bucket_files;

    /**
     * @description:  获取播放列表对应的mediaId(所有)
     * @param mediaPlayVo
     * @return: com.example.douyin_commons.core.domain.BaseResponse (data:mediaId)
     * @author zxm
     * @date: 2024/4/25 16:06
     */
    @Override
    public BaseResponse getAllPublist(MediaPlayVo mediaPlayVo) {
        // 查询数据库
        String[] mediaIdList = mediaFilesMapper.getAllMediaId();
        return BaseResponse.success(mediaIdList);
    }

    /**
     * @description: 获取播放列表对应的mediaId(根据用户id (全部))
     * @param clickPlayVo
     * @return: com.example.douyin_commons.core.domain.BaseResponse
     * @author zxm
     * @date: 2024/4/25 18:11
     */
    @Override
    public BaseResponse clickPlayList(ClickPlayVo clickPlayVo) {
        String[] mediaIdList = clickPlayVo.getMediaIdList();
        return BaseResponse.success(mediaIdList);
    }

    /**
     * @description: 根据播放列表对应的mediaId输出可播放的外链列表 （根据userId从Redis中读取，再从mysql中读取）
     * @param clickPlayVo
     * @return: com.example.douyin_commons.core.domain.BaseResponse
     * @author zxm
     * @date: 2024/4/25 22:49
     */
    @Override
    public BaseResponse getUserPlay(ClickPlayVo clickPlayVo) {
        String[] mediaIdList = clickPlayVo.getMediaIdList();
        String userId = clickPlayVo.getUserId();
        // 从Redis中获取
        Long num = redisTemplate.opsForZSet().size(RedisConstants.PUBLIST_USER_MEDIA_KEY + userId);
        log.info("num:{}",num);
        if(num == null || num.equals(0L)){
            // 读取数据库
            // TODO: 级联查询
            DyPublish[] temp_dypublish = publishMapper.selectByUserId(userId);
            String[] temp_media_url_list = mediaFilesMapper.getMediaUrlByUserId(userId);
            // 保存到Redis
            for(int i=0;i<temp_dypublish.length;i++){
                try {
                    long scope = temp_dypublish[i].getUpdateTime().getTime();
                    // 判断该数据是否已存在与redis
//                    if(redisTemplate.opsForZSet().count(RedisConstants.PUBLIST_USER_MEDIA_KEY+userId, scope, scope+1)>0){
//                        continue;
//                    }
                    // 记录到Redis中
                    String tempMediaUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_videofiles).object(temp_media_url_list[i]).method(Method.GET).build());
                    MediaPublistDTO mediaPublistDTO = new MediaPublistDTO(mediaIdList[i],tempMediaUrl);
                    zSetUtils.addObjectToZSet(RedisConstants.PUBLIST_USER_MEDIA_KEY + userId, mediaPublistDTO, scope);
//                    redisTemplate.opsForZSet().add(RedisConstants.PUBLIST_USER_MEDIA_KEY + publistVO.getUserId(), tempCoverUrl, temp_num[i].getUpdateTime().getTime());
                }catch (Exception e){
                    e.printStackTrace();
                    return BaseResponse.fail("获取外链失败");
                }
            }
            if(temp_dypublish.length>0){
                redisTemplate.expire(RedisConstants.PUBLIST_USER_MEDIA_KEY+userId, RedisConstants.PUBLIST_USER_MEDIA_TTL, TimeUnit.DAYS);
            }
        }

        // 分页读取
        Long mediaCount = redisTemplate.opsForZSet().size(RedisConstants.PUBLIST_USER_MEDIA_KEY+userId);
        Long lastId = Long.valueOf(clickPlayVo.getLastId());
        Long offset = Long.valueOf(clickPlayVo.getOffset());
        Set<ZSetOperations.TypedTuple<MediaPublistDTO>> mediaUrl = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(RedisConstants.PUBLIST_USER_MEDIA_KEY+userId, 0, lastId, offset, 5);
        if(mediaUrl==null || mediaUrl.isEmpty()){
            return BaseResponse.success();
        }
        long minTime = 0;
        int os = 1;
        // 从Redis中读取
        List<String> url = new ArrayList<>(mediaUrl.size());
//        List<String> mediaId = new ArrayList<>(mediaUrl.size());

        for (ZSetOperations.TypedTuple<MediaPublistDTO> tuple : mediaUrl) {
            try {
//                mediaId.add(tuple.getValue().getMediaId());
                log.info("tuple:{}", Objects.requireNonNull(tuple.getValue()).getMediaId());
                url.add(tuple.getValue().getMediaUrl());
            }catch (Exception e){
                e.printStackTrace();
                return BaseResponse.fail("获取外链失败");
            }
            long time = tuple.getScore().longValue();
            if (time == minTime) {
                os++;
            }else {
                minTime = time;
                os = 1;
            }
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("url", url);
//        map.put("mediaId", mediaId);
        map.put("minTime", minTime);
        map.put("offset", os);
        map.put("mediaCount", mediaCount);
        // 返回结果
        return BaseResponse.success(map);
    }


    /**
     * @description: 根据播放列表对应的mediaId输出可播放的外链列表 （根据mediaId从Redis中读取，再从mysql中读取）
     * @param urlListVo
     * @return: com.example.douyin_commons.core.domain.BaseResponse
     * @author zxm
     * @date: 2024/4/25 18:06
     */
    @Override
    public BaseResponse getMediaPlay(UrlListVo urlListVo) {
        // TODO: 默认视频流分页读取
        String[] mediaIdList = urlListVo.getMediaIdList();
        // 从Redis中获取
        Long num = redisTemplate.opsForZSet().size(RedisConstants.PUBLIST_DEFAULT_MEDIA_KEY);
        log.info("num:{}",num);
        if(num == null || num.equals(0L)){
            // 读取数据库
            // TODO: 级联
            List<MediaJoinPublish> temp_entry = mediaFilesMapper.findMediaUrlAndUpdateTime();
            // 保存到Redis
            for(int i=0;i<temp_entry.size();i++){
                try {
                    log.info("updateTime:{}",temp_entry.get(i));
                    long scope = temp_entry.get(i).getDyPublish().getUpdateTime().getTime();
                    // 记录到Redis中
                    String tempMediaUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_videofiles).object(temp_entry.get(i).getMediaUrl()).method(Method.GET).build());
                    MediaPublistDTO mediaPublistDTO = new MediaPublistDTO(mediaIdList[i],tempMediaUrl);
                    zSetUtils.addObjectToZSet(RedisConstants.PUBLIST_DEFAULT_MEDIA_KEY, mediaPublistDTO, scope);
//                    redisTemplate.opsForZSet().add(RedisConstants.PUBLIST_USER_MEDIA_KEY + publistVO.getUserId(), tempCoverUrl, temp_num[i].getUpdateTime().getTime());
                }catch (Exception e){
                    e.printStackTrace();
                    return BaseResponse.fail("获取外链失败");
                }
            }
            if(temp_entry.size()>0){
                redisTemplate.expire(RedisConstants.PUBLIST_DEFAULT_MEDIA_KEY, RedisConstants.PUBLIST_DEFAULT_MEDIA_TTL, TimeUnit.DAYS);
            }
        }

        // 分页读取
        Long mediaCount = redisTemplate.opsForZSet().size(RedisConstants.PUBLIST_DEFAULT_MEDIA_KEY);
        Long lastId = Long.valueOf(urlListVo.getLastId());
        Long offset = Long.valueOf(urlListVo.getOffset());
        Set<ZSetOperations.TypedTuple<MediaPublistDTO>> mediaUrl = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(RedisConstants.PUBLIST_DEFAULT_MEDIA_KEY, 0, lastId, offset, 5);
        if(mediaUrl==null || mediaUrl.isEmpty()){
            return BaseResponse.success();
        }
        long minTime = 0;
        int os = 1;
        // 从Redis中读取
        List<String> url = new ArrayList<>(mediaUrl.size());

        for (ZSetOperations.TypedTuple<MediaPublistDTO> tuple : mediaUrl) {
            try {
//                mediaId.add(tuple.getValue().getMediaId());
                log.info("tuple:{}", Objects.requireNonNull(tuple.getValue()).getMediaId());
                url.add(tuple.getValue().getMediaUrl());
            }catch (Exception e){
                e.printStackTrace();
                return BaseResponse.fail("获取外链失败");
            }
            long time = tuple.getScore().longValue();
            if (time == minTime) {
                os++;
            }else {
                minTime = time;
                os = 1;
            }
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("url", url);
//        map.put("mediaId", mediaId);
        map.put("minTime", minTime);
        map.put("offset", os);
        map.put("mediaCount", mediaCount);
        // 返回结果
        return BaseResponse.success(map);
    }

}
