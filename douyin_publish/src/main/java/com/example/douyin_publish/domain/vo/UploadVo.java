package com.example.douyin_publish.domain.vo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author : zxm
 * @date: 2024/4/13 - 16:29
 * @Description: com.example.douyin_publish.domain.vo
 * @version: 1.0
 */
@Data
public class UploadVo {
    // 用户id
    private String uid;
    //任务ID
    private String id;
    //总分片数量
    private int chunks;
    //当前为第几块分片
    private int chunk;
    //当前分片大小
    private long size = 0L;
    //文件名
    private String name;
    //分片对象
    private MultipartFile file;
    // MD5
    private String md5;
    // 视频id
    private String mediaId;
}
