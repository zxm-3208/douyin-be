package com.example.douyin_publish.service.impl;

import com.example.douyin_commons.constant.Constants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_publish.domain.po.DyPublish;
import com.example.douyin_publish.domain.vo.PublistVO;
import com.example.douyin_publish.mapper.MediaFilesMapper;
import com.example.douyin_publish.mapper.PublishMapper;
import com.example.douyin_publish.service.ShowlistService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

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
    private MinioClient minioClient;

    // 普通文件桶
    @Value("${minio.bucket.files}")
    private String bucket_files;

    @Override
    public BaseResponse showPublist(PublistVO publistVO) {

        String userId = publistVO.getUserId();
        log.info("userId:{}",userId);
        // 查询数据库
        DyPublish[] imgUrl = publishMapper.selectByUserId(userId);

        // 获取外链
        String[] url = new String[imgUrl.length];
        String[] mediaId = new String[imgUrl.length];
        try {
            for(int i=0;i< imgUrl.length;i++) {
                mediaId[i] = imgUrl[i].getMediaId();
                url[i] = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_files).object(imgUrl[i].getImgUrl()).method(Method.GET).build());
            }
        }catch (Exception e){
            e.printStackTrace();
            return BaseResponse.fail("获取外链失败");
        }

        log.info("media:{}", mediaId);

        HashMap<String, Object> map = new HashMap<>();
        map.put("url", url);
        map.put("mediaId", mediaId);
        // 返回结果
        return BaseResponse.success(map);
    }

    @Override
    public BaseResponse clickPlay(String mediaId) {
        // 查询数据库
        String tempUrl = mediaFilesMapper.getUrlByMediaId(mediaId);
        // 获取外链
        String url = null;
        try {
            url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_files).object(tempUrl).method(Method.GET).build());
        }catch (Exception e){
            e.printStackTrace();
            return BaseResponse.fail("获取外链失败");
        }
        // 返回结果
        return BaseResponse.success(url);
    }
}
