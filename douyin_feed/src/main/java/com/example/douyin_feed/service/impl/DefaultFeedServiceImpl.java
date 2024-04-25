package com.example.douyin_feed.service.impl;

import com.example.douyin_commons.constant.RedisConstants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_commons.core.exception.MsgException;
import com.example.douyin_feed.domain.vo.ClickPlayVo;
import com.example.douyin_feed.domain.vo.MediaPlayVo;
import com.example.douyin_feed.domain.vo.UrlListVo;
import com.example.douyin_feed.mapper.MediaFilesMapper;
import com.example.douyin_feed.mapper.PublishMapper;
import com.example.douyin_feed.service.DefaultFeedService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
        // TODO: 分页
//        // 查询Redis
//        Set<Object> keys = redisTemplate.keys(RedisConstants.PUBLIST_USER_KEY+"*");
//        return BaseResponse.success(keys);
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
        // TODO: 分页
        // TODO: 1. 根据userId在Redis中获取相应视频播放列表（分页读取）。 2. 根据mediaId定位视频在播放列表位置。 3. 返回播放列表对应的mediaId
        String userId = clickPlayVo.getUserId();
        String[] mediaIdList = clickPlayVo.getMediaIdList();
//        // 取一个用户发布的视频列表，可以用Redis存储最新视频，如果当需要访问的视频不在Redis中时，在从Mysql中读取。
//        ArrayList<String> values = new ArrayList<>();
//        for(int i=0;i<mediaIdList.length;i++) {
//            if (redisTemplate.opsForHash().hasKey(RedisConstants.PUBLIST_USER_KEY + clickPlayVo.getUserId() + ":" + mediaIdList[i], "mediaUrl")) {
//                values.add((String) redisTemplate.opsForHash().get(RedisConstants.PUBLIST_USER_KEY + clickPlayVo.getUserId() + ":" + mediaIdList[i], "mediaUrl"));
//            }
//            else{
//                HashMap<String, String> url_map = getExtUrl(mediaIdList[i], RedisConstants.PUBLIST_USER_KEY, RedisConstants.PUBLIST_USER_TTL);
//                values.add(url_map.get("mediaUrl"));
//            }
//        }
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
        // 获取Redis Keys
        String[] mediaIdList = clickPlayVo.getMediaIdList();
        // 取一个用户发布的视频列表，可以用Redis存储最新视频，如果当需要访问的视频不在Redis中时，在从Mysql中读取。
        ArrayList<String> values = new ArrayList<>();
        for(int i=0;i<mediaIdList.length;i++) {
            if (redisTemplate.opsForHash().hasKey(RedisConstants.PUBLIST_USER_KEY + clickPlayVo.getUserId() + ":" + mediaIdList[i], "mediaUrl")) {
                values.add((String) redisTemplate.opsForHash().get(RedisConstants.PUBLIST_USER_KEY + clickPlayVo.getUserId() + ":" + mediaIdList[i], "mediaUrl"));
            }
            else{
                HashMap<String, String> url_map = getExtUrl(mediaIdList[i], RedisConstants.PUBLIST_USER_KEY, RedisConstants.PUBLIST_USER_TTL);
                values.add(url_map.get("mediaUrl"));
            }
        }
        return BaseResponse.success(values);
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
        // 获取Redis Keys
        ArrayList<String> mediaId = urlListVo.getMediaIdList();
        log.info("mediaId:{}",mediaId);
        // 获取Redis value
        ArrayList<String> values = new ArrayList<>();
        for(int i=0;i<mediaId.size();i++) {
            if(redisTemplate.opsForHash().hasKey(RedisConstants.PUBLIST_MEDIAID_KEY+mediaId.get(i), "mediaUrl")) {
                values.add((String) redisTemplate.opsForHash().get(RedisConstants.PUBLIST_MEDIAID_KEY+mediaId.get(i), "mediaUrl"));
            }
            else{
                HashMap<String, String> url_map = getExtUrl(mediaId.get(i), RedisConstants.PUBLIST_MEDIAID_KEY, RedisConstants.PUBLIST_MEDIAID_TTL);
                values.add(url_map.get("mediaUrl"));
            }
        }
        return BaseResponse.success(values);
    }

    /**
     * @description: 从数据库中读取封面和视频url，并转换为外链保存到Redis中
     * @param mediaId
     * @return: java.util.HashMap<java.lang.String,java.lang.String>
     * @author zxm
     * @date: 2024/4/25 21:27
     */
    private HashMap<String, String> getExtUrl(String mediaId, String mediaIdPrefix, Long ttlPrefix){
        // redis中不存在，从数据库中获取并缓存到Redis中
        log.info("mediaId:{}",mediaId);
        // 查询数据库获取mediaUrl和coverUrl
        String temp_cover_url = publishMapper.getCoverUrlByid(mediaId);
        String temp_media_url = mediaFilesMapper.getMediaUrlByid(mediaId);

        log.info("封面url;为：{}",temp_cover_url);
        log.info("视频url为：{}",temp_media_url);

        String cover_url = null;
        String media_url = null;
        try {
            cover_url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_files).object(temp_cover_url).method(Method.GET).build());
            media_url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_videofiles).object(temp_media_url).method(Method.GET).build());
        }catch (Exception e) {
            e.printStackTrace();
            log.error("获取外链失败");
            return null;
        }
        log.info("封面外链为：{}",cover_url);
        log.info("视频外链为：{}",media_url);

        // 将发布的视频推送给Redis (key: 用户id, map:{media_url:xxx, cover_url:xxx})
        Map<String, String> mediaMap = new HashMap<>();
        mediaMap.put(RedisConstants.MEDIA_URL_KEY, media_url);
        mediaMap.put(RedisConstants.COVER_URL_KEY, cover_url);
        redisTemplate.opsForHash().putAll(mediaIdPrefix+mediaId,mediaMap);
        redisTemplate.expire(mediaIdPrefix+mediaId, ttlPrefix, TimeUnit.DAYS);

        HashMap<String, String> urlMap = new HashMap<>();
        urlMap.put("mediaUrl", media_url);
        urlMap.put("coverUrl", cover_url);
        return urlMap;
    }

}
