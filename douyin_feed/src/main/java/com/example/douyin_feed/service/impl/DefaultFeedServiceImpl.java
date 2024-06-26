package com.example.douyin_feed.service.impl;

import com.example.douyin_commons.constant.RedisConstants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_commons.core.domain.MediaPublistDTO;
import com.example.douyin_feed.domain.dto.LikeFeedDTO;
import com.example.douyin_feed.domain.po.DyMedia;
import com.example.douyin_feed.domain.po.DyUser;
import com.example.douyin_feed.domain.po.DyUserLikeMedia;
import com.example.douyin_feed.domain.vo.ClickPlayVo;
import com.example.douyin_feed.domain.vo.UrlListVo;
import com.example.douyin_feed.mapper.master.MediaFilesMapper;
import com.example.douyin_feed.mapper.second.DyUserLikeMediaMapper;
import com.example.douyin_feed.mapper.second.DyUserMapper;
import com.example.douyin_feed.service.DefaultFeedService;
import com.example.douyin_feed.utils.ZSetUtils;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
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
// Feed流需要返回mediaIdList
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
    private DyUserLikeMediaMapper dyUserLikeMediaMapper;
    @Autowired
    private ZSetUtils zSetUtils;
    @Autowired
    private DyUserMapper dyUserMapper;
    // 视频文件桶
    @Value("${minio.bucket.videofiles}")
    private String bucket_videofiles;

    @Value("${minio.bucket.icon}")
    private String bucket_icon_file;

    /**
     * @description:  获取播放列表对应的mediaId(所有)
     * @return: com.example.douyin_commons.core.domain.BaseResponse (data:mediaId)
     * @author zxm
     * @date: 2024/4/25 16:06
     */
    @Override
    public BaseResponse getAllPublist() {
        List<String> mediaIdList = new ArrayList<>();
//        List userIdList = new ArrayList();
//        List mediaTitleList = new ArrayList();
//        List urlList = new ArrayList();
        // 查询数据库
        List<DyMedia> temp_entry = mediaFilesMapper.findMediaUrlAndUpdateTimeByStatus("1");
        log.info("查询到的数据有{}条", temp_entry.size());
        // 记录meidaIdList数据
        for(DyMedia x: temp_entry){
            mediaIdList.add(x.getId());
        }
        if(temp_entry.size()!=zSetUtils.getObjectSize(RedisConstants.PUBLIST_DEFAULT_MEDIA_KEY)) {
            // 删除Redis
            if(redisTemplate.opsForZSet().size(RedisConstants.PUBLIST_DEFAULT_MEDIA_KEY)>0)
                zSetUtils.delAllObjectToZSet(RedisConstants.PUBLIST_DEFAULT_MEDIA_KEY);
            // 保存到Redis
            for (int i = 0; i < temp_entry.size(); i++) {
                try {
                    log.info("updateTime:{}", temp_entry.get(i));
                    long scope = temp_entry.get(i).getDyPublish().getUpdateTime().getTime();
                    String userId = temp_entry.get(i).getDyPublish().getAuthor();
                    String title = temp_entry.get(i).getDyPublish().getTitle();
                    String userName = temp_entry.get(i).getDyUser().getUserName();
                    String iconUrl = temp_entry.get(i).getDyUser().getIcon();
                    // 记录到Redis中
                    MediaPublistDTO mediaPublistDTO = new MediaPublistDTO(temp_entry.get(i).getId(), temp_entry.get(i).getMediaUrl(), userId, title, userName, iconUrl);
                    zSetUtils.addObjectToZSet(RedisConstants.PUBLIST_DEFAULT_MEDIA_KEY, mediaPublistDTO, scope);
                } catch (Exception e) {
                    e.printStackTrace();
                    return BaseResponse.fail("获取外链失败");
                }
            }
            if (temp_entry.size() > 0) {
                redisTemplate.expire(RedisConstants.PUBLIST_DEFAULT_MEDIA_KEY, RedisConstants.PUBLIST_DEFAULT_MEDIA_TTL, TimeUnit.DAYS);
            }
        }

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

        return BaseResponse.success();
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
        String userId = clickPlayVo.getUserId();
        List mediaIdList = new ArrayList();
        List userIdList = new ArrayList();
        List mediaTitleList = new ArrayList();
        List userNameList = new ArrayList();
        List userIconList = new ArrayList();
        DyUser user = null;
        // 从Redis中获取
        Long num = redisTemplate.opsForZSet().size(RedisConstants.PUBLIST_USER_MEDIA_KEY + userId);
        log.info("num:{}",num);
        if(num == null || num.equals(0L)){
            // 读取数据库
            // 级联查询
            List<DyMedia> temp_entry = mediaFilesMapper.findMediaUrlAndUpdateTimeByUserId(userId);
            user = dyUserMapper.getUserdById(userId);
            // 保存到Redis
            for(int i=0;i<temp_entry.size();i++){
                try {
                    long scope = temp_entry.get(i).getDyPublish().getUpdateTime().getTime();
                    String mediaId = temp_entry.get(i).getId();
                    String title =  temp_entry.get(i).getDyPublish().getTitle();
                    String userName = temp_entry.get(i).getDyUser().getUserName();
                    String iconUrl = temp_entry.get(i).getDyUser().getIcon();
                    // 获取外链
                    MediaPublistDTO mediaPublistDTO = new MediaPublistDTO(mediaId,temp_entry.get(i).getMediaUrl(),userId,title, userName, temp_entry.get(i).getDyUser().getIcon());
                    log.info("DTO:{}", mediaPublistDTO);
                    zSetUtils.addObjectToZSet(RedisConstants.PUBLIST_USER_MEDIA_KEY + userId, mediaPublistDTO, scope);
                    redisTemplate.expire(RedisConstants.PUBLIST_USER_MEDIA_KEY + userId, RedisConstants.PUBLIST_USER_MEDIA_TTL, TimeUnit.DAYS);
//                    redisTemplate.opsForZSet().add(RedisConstants.PUBLIST_USER_MEDIA_KEY + publistVO.getUserId(), tempCoverUrl, temp_num[i].getUpdateTime().getTime());
                }catch (Exception e){
                    e.printStackTrace();
                    return BaseResponse.fail("获取外链失败");
                }
            }
            if(temp_entry.size()>0){
                redisTemplate.expire(RedisConstants.PUBLIST_USER_MEDIA_KEY+userId, RedisConstants.PUBLIST_USER_MEDIA_TTL, TimeUnit.DAYS);
            }
        }

        // 分页读取
        Long mediaCount = redisTemplate.opsForZSet().size(RedisConstants.PUBLIST_USER_MEDIA_KEY+userId);
        Long lastId = Long.valueOf(clickPlayVo.getLastId());
        Long offset = Long.valueOf(clickPlayVo.getOffset());
        Set<ZSetOperations.TypedTuple<MediaPublistDTO>> mediaUrl = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(RedisConstants.PUBLIST_USER_MEDIA_KEY+userId, 0, lastId, offset, 10);
        if(mediaUrl==null || mediaUrl.isEmpty()){
            return BaseResponse.success();
        }
        long minTime = 0;
        int os = 1;
        // 从Redis中读取
        List<String> url = new ArrayList<>(mediaUrl.size());

        for (ZSetOperations.TypedTuple<MediaPublistDTO> tuple : mediaUrl) {
            try {
                log.info("tuple:{}", tuple.getValue());
                userIdList.add(tuple.getValue().getUserId());
                mediaTitleList.add(tuple.getValue().getMediaTitle());
                mediaIdList.add(tuple.getValue().getMediaId());
                userNameList.add(tuple.getValue().getUserName());
                String tempIconUrl = null;
                if(tuple.getValue().getUserIcon()!=null)
                    tempIconUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_icon_file).object(tuple.getValue().getUserIcon()).method(Method.GET).build());
                // 记录到Redis中
                String tempMediaUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_videofiles).object(tuple.getValue().getMediaUrl()).method(Method.GET).build());
                userIconList.add(tempIconUrl);
                url.add(tempMediaUrl);
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
        map.put("mediaId", mediaIdList);
        map.put("minTime", minTime);
        map.put("offset", os);
        map.put("mediaCount", mediaCount);
        map.put("userId", userIdList);
        map.put("mediaTitle", mediaTitleList);
        map.put("userName", userNameList);
        map.put("userIcon", userIconList);
        // 返回结果
        return BaseResponse.success(map);
    }

    // TODO: 三表级联
    @Override
    public BaseResponse getUserLikePlay(ClickPlayVo clickPlayVo) {
        String userId = clickPlayVo.getUserId();
        List userIdList = new ArrayList();
        List mediaTitleList = new ArrayList();
        // 从Redis中获取
        Long num = redisTemplate.opsForZSet().size(RedisConstants.USER_LIKE_MEDIA_LIST_KEY + userId);
        List mediaIdList = new ArrayList();
        List userNameList = new ArrayList();
        List userIconList = new ArrayList();
        log.info("num:{}",num);
        if(num == null || num.equals(0L)){
            // 读取数据库
            // 级联
            // 现在like表中查mediaId, 再在Publish表中获取url，最后再转化为外链
            log.info("userId:{}",userId);
            List<DyMedia> dyMedia = mediaFilesMapper.findMediaUserPublishAndLikeByUserId(userId);
            for(DyMedia x: dyMedia)
                log.info("dyMedia:{}",x);
            log.info("size:{}",dyMedia.size());
            // 保存到Redis
            for(int i=0;i<dyMedia.size();i++){
                try {
                    log.info("updateTime:{}",dyMedia.get(i).getDyUserLikeMedia().getLikeUpdateTime());
                    log.info("time:{},{}", dyMedia.get(i).getDyUserLikeMedia().getLikeUpdateTime(), dyMedia.get(i).getDyUserLikeMedia().getLikeUpdateTime().getTime());
                    long scope = dyMedia.get(i).getDyUserLikeMedia().getLikeUpdateTime().getTime();
                    String title = dyMedia.get(i).getDyPublish().getTitle();
                    String authorId = dyMedia.get(i).getDyPublish().getAuthor();
                    String userName = dyMedia.get(i).getDyUser().getUserName();
                    String iconUrl = dyMedia.get(i).getDyUser().getIcon();
                    MediaPublistDTO mediaPublistDTO = new MediaPublistDTO(dyMedia.get(i).getId(),dyMedia.get(i).getMediaUrl(), authorId, title,userName,dyMedia.get(i).getDyUser().getIcon());
                    log.info("保存到Redis ZSet中：{}",i);
                    zSetUtils.addObjectToZSet(RedisConstants.USER_LIKE_MEDIA_LIST_KEY + userId, mediaPublistDTO, scope);
                }catch (Exception e){
                    e.printStackTrace();
                    return BaseResponse.fail("获取外链失败");
                }
            }
            if(dyMedia.size()>0){
                log.info("Redis设置过期时间");
                redisTemplate.expire(RedisConstants.USER_LIKE_MEDIA_LIST_KEY + userId, RedisConstants.USER_LIKE_MEDIA_LIST_TTL, TimeUnit.DAYS);
            }
        }

        // 分页读取
        Long mediaCount = redisTemplate.opsForZSet().size(RedisConstants.USER_LIKE_MEDIA_LIST_KEY + userId);
        Long lastId = Long.valueOf(clickPlayVo.getLastId());
        Long offset = Long.valueOf(clickPlayVo.getOffset());
        Set<ZSetOperations.TypedTuple<MediaPublistDTO>> mediaUrl = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(RedisConstants.USER_LIKE_MEDIA_LIST_KEY + userId, 0, lastId, offset, 10);
        if(mediaUrl==null || mediaUrl.isEmpty()){
            return BaseResponse.success();
        }
        long minTime = 0;
        int os = 1;
        // 从Redis中读取
        List<String> url = new ArrayList<>(mediaUrl.size());

        for (ZSetOperations.TypedTuple<MediaPublistDTO> tuple : mediaUrl) {
            try {
                // mediaId.add(tuple.getValue().getMediaId());
                log.info("tuple:{}", tuple.getValue());
                mediaIdList.add(tuple.getValue().getMediaId());
                userIdList.add(tuple.getValue().getUserId());
                mediaTitleList.add(tuple.getValue().getMediaTitle());
                userNameList.add(tuple.getValue().getUserName());
                String tempIconUrl= null;
                if(tuple.getValue().getUserIcon()!=null) {
                    tempIconUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_icon_file).object(tuple.getValue().getUserIcon()).method(Method.GET).build());
                }
                userIconList.add(tempIconUrl);
                String tempMediaUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_videofiles).object(tuple.getValue().getMediaUrl()).method(Method.GET).build());
                log.info("url:{}",tempMediaUrl);
                url.add(tempMediaUrl);
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
        map.put("mediaId", mediaIdList);
        map.put("minTime", minTime);
        map.put("offset", os);
        map.put("mediaCount", mediaCount);
        map.put("userId", userIdList);
        map.put("mediaTitle", mediaTitleList);
        map.put("userName", userNameList);
        map.put("userIcon", userIconList);
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
    // TODO: 三表级联
    @Override
    public BaseResponse getMediaPlay(UrlListVo urlListVo) {
        // 从Redis中获取
        Long num = redisTemplate.opsForZSet().size(RedisConstants.PUBLIST_DEFAULT_MEDIA_KEY);
        List mediaIdList = new ArrayList();
        List userIdList = new ArrayList();
        List mediaTitleList = new ArrayList();
        List userNameList = new ArrayList();
        List userIconList = new ArrayList();
        log.info("num:{}",num);
        if(num == null || num.equals(0L)){
            // 读取数据库
            // 级联 //
            List<DyMedia> temp_entry = mediaFilesMapper.findMediaUrlAndUpdateTimeByUserId(urlListVo.getUserId());
            // 保存到Redis
            for(int i=0;i<temp_entry.size();i++){
                try {
                    long scope = temp_entry.get(i).getDyPublish().getUpdateTime().getTime();
                    String mediaTitle = temp_entry.get(i).getDyPublish().getTitle();
                    String userId = temp_entry.get(i).getDyPublish().getAuthor();
                    String userName = temp_entry.get(i).getDyUser().getUserName();
                    String iconUrl = temp_entry.get(i).getDyUser().getIcon();
                    // 记录到Redis中
                    MediaPublistDTO mediaPublistDTO = new MediaPublistDTO(temp_entry.get(i).getId(),temp_entry.get(i).getMediaUrl(), userId, mediaTitle, userName, temp_entry.get(i).getDyUser().getIcon());
                    zSetUtils.addObjectToZSet(RedisConstants.PUBLIST_DEFAULT_MEDIA_KEY, mediaPublistDTO, scope);
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
        Set<ZSetOperations.TypedTuple<MediaPublistDTO>> mediaUrl = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(RedisConstants.PUBLIST_DEFAULT_MEDIA_KEY, 0, lastId, offset, 10);
        if(mediaUrl==null || mediaUrl.isEmpty()){
            return BaseResponse.success();
        }
        long minTime = 0;
        int os = 1;
        // 从Redis中读取
        List<String> url = new ArrayList<>(mediaUrl.size());

        for (ZSetOperations.TypedTuple<MediaPublistDTO> tuple : mediaUrl) {
            try {
                // mediaId.add(tuple.getValue().getMediaId());
                log.info("tuple:{}", tuple.getValue());
                mediaIdList.add(tuple.getValue().getMediaId());
                userIdList.add(tuple.getValue().getUserId());
                mediaTitleList.add(tuple.getValue().getMediaTitle());
                String tempIconUrl= null;
                if(tuple.getValue().getUserIcon()!=null) {
                    tempIconUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_icon_file).object(tuple.getValue().getUserIcon()).method(Method.GET).build());
                }
                String tempMediaUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_videofiles).object(tuple.getValue().getMediaUrl()).method(Method.GET).build());
                url.add(tempMediaUrl);
                userNameList.add(tuple.getValue().getUserName());
                userIconList.add(tempIconUrl);
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
        log.info("userIdList:{}", userIdList);
        HashMap<String, Object> map = new HashMap<>();
        map.put("url", url);
        map.put("mediaId", mediaIdList);
        map.put("minTime", minTime);
        map.put("offset", os);
        map.put("mediaCount", mediaCount);
        map.put("userId", userIdList);
        map.put("mediaTitle", mediaTitleList);
        map.put("userName", userNameList);
        map.put("userIcon", userIconList);
        // 返回结果
        return BaseResponse.success(map);
    }

}
