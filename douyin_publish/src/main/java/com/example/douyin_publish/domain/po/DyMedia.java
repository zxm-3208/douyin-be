package com.example.douyin_publish.domain.po;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : zxm
 * @date: 2024/4/9 - 12:05
 * @Description: com.example.douyin_publish.domain.po
 * @version: 1.0
 */
@Data
public class DyMedia implements Serializable {
    private static final long serialVersionUID = 118414433334990999L;
    /**
     * 主键
     */
    private String id;
    /**
     * 视频url
     */
    private String mediaUrl;
    /**
     * 状态
     */
    private String status;

    /**
     * MD5
     */
    private String md5;

}

