package com.example.douyin_publish.service.impl;

import com.example.douyin_commons.constant.Constants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_publish.domain.po.DyPublish;
import com.example.douyin_publish.domain.vo.PublistVO;
import com.example.douyin_publish.mapper.PublishMapper;
import com.example.douyin_publish.service.ShowlistService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    private MinioClient minioClient;

    // 普通文件桶
    @Value("${minio.bucket.files}")
    private String bucket_files;

    @Override
    public BaseResponse showPublist(PublistVO publistVO) {

        String userId = publistVO.getUserId();
        log.info("userId:{}",userId);
        // 查询数据库
        String[] imgUrl = publishMapper.selectByUserId(userId);

        log.info("imgURL{}",imgUrl);

        // 获取外链
        String[] url = new String[imgUrl.length];
        try {
            for(int i=0;i< imgUrl.length;i++) {
                url[i] = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket_files).object(imgUrl[i]).method(Method.GET).build());
            }
        }catch (Exception e){
            e.printStackTrace();
            return BaseResponse.fail("获取外链失败");
        }
        log.info("URL{}",url);
        // 返回结果
        return BaseResponse.success(url);
    }
}
