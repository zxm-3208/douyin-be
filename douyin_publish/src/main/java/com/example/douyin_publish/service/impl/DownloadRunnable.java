package com.example.douyin_publish.service.impl;

import com.example.douyin_commons.core.exception.MsgException;
import com.example.douyin_publish.service.UploadService;
import com.example.douyin_publish.utils.SpringContextUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

import static com.example.douyin_publish.service.impl.UploadServiceImpl.files;

/**
 * @author : zxm
 * @date: 2024/4/18 - 15:04
 * @Description: com.example.douyin_publish.service.impl
 * @version: 1.0
 */
@Data
@Slf4j
public class DownloadRunnable implements Runnable{

    private String chunkFileFolderPath;

    private int chunk_index;

    private String bucket_name;

    private UploadService uploadService;

    private int chunkTotal;

    public DownloadRunnable(String chunkFileFolderPath, int chunk_index, String bucket_name, int chunkTotal) {
        this.chunkFileFolderPath = chunkFileFolderPath;
        this.chunk_index = chunk_index;
        this.bucket_name = bucket_name;
        this.chunkTotal = chunkTotal;
    }

    @Override
    public void run() {
        String chunkFilePath = chunkFileFolderPath + chunk_index;
        // 下载文件
        File chunkFile = null;
        try{
            chunkFile = File.createTempFile("chunk" + chunk_index, null);
        }catch (IOException e){
            e.printStackTrace();
            MsgException.cast("下载分块时创建临时文件出错");
        }
        log.info("++{},{}",bucket_name,chunkFilePath);
        // 手动注入
        uploadService = SpringContextUtils.getApplicationContext().getBean(UploadService.class);
        uploadService.downloadFileFromMinIO(chunkFile, bucket_name, chunkFilePath);
        files[chunk_index] = chunkFile;
        //每执行一次数值减少一
        uploadService.countDown();
    }
}
