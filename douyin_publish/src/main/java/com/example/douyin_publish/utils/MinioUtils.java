package com.example.douyin_publish.utils;

import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author : zxm
 * @date: 2024/4/17 - 19:02
 * @Description: com.example.douyin_publish.utils
 * @version: 1.0
 */
@Component
public class MinioUtils {

    @Autowired
    private MinioClient minioClient;

    /**
     * 判断文件是否存在
     */
    public Boolean checkFileIsExist(String bucket, String objectName){
        try {
            minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(objectName).build()
            );
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
