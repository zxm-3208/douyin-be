package com.example.douyin_publish.service.impl;

import com.example.douyin_commons.constant.RedisConstants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_commons.core.domain.CoverPublistDTO;
import com.example.douyin_publish.domain.dto.UserLikeMediaDTO;
import com.example.douyin_publish.domain.po.DyPublish;
import com.example.douyin_publish.domain.po.DyUserLikeMedia;
import com.example.douyin_publish.domain.vo.PublistVO;
import com.example.douyin_publish.mapper.master.MediaFilesMapper;
import com.example.douyin_publish.mapper.master.PublishMapper;
import com.example.douyin_publish.mapper.second.DyUserLikeMediaMapper;
import com.example.douyin_publish.service.ShowlistService;
import com.example.douyin_publish.utils.ZSetUtils;
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
 * @date: 2024/4/23 - 14:21
 * @Description: com.example.douyin_publish.service.impl
 * @version: 1.0
 */
@Service
@Slf4j
public class ShowlistServiceImpl implements ShowlistService {

    @Autowired
    PublishMapper publishMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    DyUserLikeMediaMapper dyUserLikeMediaMapper;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ZSetUtils zSetUtils;

    // 普通文件桶
    @Value("${minio.bucket.files}")
    private String bucket_files;

    @Override
    public BaseResponse showPublist(PublistVO publistVO) {

        String userId = publistVO.getUserId();

        // 查询视频列表视频总数Redis
        Long num = redisTemplate.opsForZSet().size(RedisConstants.PUBLIST_USER_COVER_KEY + publistVO.getUserId());

        // 判断总数是否与Redis缓存中的视频url数量一致
        // 不一致
        if(num==null || num.equals(0L)){
            // 读取数据库
            DyPublish[] temp_num = publishMapper.selectByUserId(userId);
            System.out.println(temp_num.length);
            // 保存到Redis
            for(int i=0;i<temp_num.length;i++){
                try {
                    long scope = temp_num[i].getUpdateTime().getTime();
                    CoverPublistDTO coverPublistDTO = new CoverPublistDTO(temp_num[i].getMediaId(),temp_num[i].getImgUrl());
                    zSetUtils.addObjectToZSet(RedisConstants.PUBLIST_USER_COVER_KEY + publistVO.getUserId(), coverPublistDTO, scope);
                }catch (Exception e){
                    e.printStackTrace();
                    return BaseResponse.fail("获取外链失败");
                }
            }
            if(temp_num.length>0){
                redisTemplate.expire(RedisConstants.PUBLIST_USER_COVER_KEY+publistVO.getUserId(), RedisConstants.PUBLIST_USER_COVER_TTL, TimeUnit.DAYS);
            }
        }

        // 分页读取
        Long mediaCount = redisTemplate.opsForZSet().size(RedisConstants.PUBLIST_USER_COVER_KEY+publistVO.getUserId());
        Long lastId = Long.valueOf(publistVO.getLastId());
        Long offset = Long.valueOf(publistVO.getOffset());
        Set<ZSetOperations.TypedTuple<CoverPublistDTO>> imgUrl = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(RedisConstants.PUBLIST_USER_COVER_KEY+publistVO.getUserId(), 0, lastId, offset, 100);
        log.info("{}",imgUrl);
        if(imgUrl==null || imgUrl.isEmpty()){
            return BaseResponse.success();
        }
        long minTime = 0;
        int os = 1;
        // 从Redis中读取
        List<String> url = new ArrayList<>(imgUrl.size());
        List<String> mediaId = new ArrayList<>(imgUrl.size());

        for (ZSetOperations.TypedTuple<CoverPublistDTO> tuple : imgUrl) {
            try {
                mediaId.add(tuple.getValue().getMediaId());
                String tempCoverUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_files).object(tuple.getValue().getCoverUrl()).method(Method.GET).build());
                url.add(tempCoverUrl);
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
        map.put("mediaId", mediaId);
        map.put("minTime", minTime);
        map.put("offset", os);
        map.put("mediaCount", mediaCount);
        // 返回结果
        return BaseResponse.success(map);
//        return null;
    }

    // Redis存储格式
    @Override
    public BaseResponse showLikeList(PublistVO publistVO) {
        String userId = publistVO.getUserId();

        // 查询视频列表视频总数Redis
        Long num = redisTemplate.opsForZSet().size(RedisConstants.LIKE_USER_COVER_KEY + publistVO.getUserId());

        // 判断总数是否与Redis缓存中的视频url数量一致
        // 不一致
        if(num==null || num.equals(0L)){
            // 读取数据库
            List<DyPublish> temp_num = publishMapper.findUrl();
            List<DyUserLikeMedia> dyUserLikeMedia = dyUserLikeMediaMapper.selectByUserId(userId);
            // 提取mediaIdList和coverUrl
            List<UserLikeMediaDTO> tempList = new ArrayList<>();
            for(DyPublish x : temp_num){
                for(DyUserLikeMedia y : dyUserLikeMedia){
                    if(y.getMediaid().equals(x.getMediaId())){
                        tempList.add(new UserLikeMediaDTO(x.getMediaId(), y.getLikeUpdateTime(), x.getImgUrl()));
                    }
                }
            }
            // 保存到Redis
            for(int i=0;i<tempList.size();i++){
                try {
                    long scope = tempList.get(i).getUpdateTime().getTime();
                    // 记录到Redis中
                    CoverPublistDTO coverPublistDTO = new CoverPublistDTO(tempList.get(i).getMediaId(),tempList.get(i).getCoverUrl());
                    System.out.println(coverPublistDTO.getCoverUrl());
                    zSetUtils.addObjectToZSet(RedisConstants.LIKE_USER_COVER_KEY + publistVO.getUserId(), coverPublistDTO, scope);
                }catch (Exception e){
                    e.printStackTrace();
                    return BaseResponse.fail("获取外链失败");
                }
            }
            if(tempList.size()>0){
                redisTemplate.expire(RedisConstants.LIKE_USER_COVER_KEY+publistVO.getUserId(), RedisConstants.LIKE_USER_COVER_TTL, TimeUnit.DAYS);
            }
        }

        // 分页读取
        Long mediaCount = redisTemplate.opsForZSet().size(RedisConstants.LIKE_USER_COVER_KEY+publistVO.getUserId());
        Long lastId = Long.valueOf(publistVO.getLastId());
        Long offset = Long.valueOf(publistVO.getOffset());
        Set<ZSetOperations.TypedTuple<CoverPublistDTO>> imgUrl = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(RedisConstants.LIKE_USER_COVER_KEY+publistVO.getUserId(), 0, lastId, offset, 100);
        if(imgUrl==null || imgUrl.isEmpty()){
            return BaseResponse.success();
        }
        long minTime = 0;
        int os = 1;
        // 从Redis中读取
        List<String> url = new ArrayList<>(imgUrl.size());
        List<String> mediaId = new ArrayList<>(imgUrl.size());

        for (ZSetOperations.TypedTuple<CoverPublistDTO> tuple : imgUrl) {
            try {
                mediaId.add(tuple.getValue().getMediaId());
                String tempCoverUrl = null;
                if(tuple.getValue().getCoverUrl()!=null){
                    tempCoverUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_files).object(tuple.getValue().getCoverUrl()).method(Method.GET).build());

                }
                url.add(tempCoverUrl);
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
        map.put("mediaId", mediaId);
        map.put("minTime", minTime);
        map.put("offset", os);
        map.put("mediaCount", mediaCount);
        // 返回结果
        return BaseResponse.success(map);
    }
}
