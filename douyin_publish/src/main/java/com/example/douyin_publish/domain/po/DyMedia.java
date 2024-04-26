package com.example.douyin_publish.domain.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author : zxm
 * @date: 2024/4/9 - 12:05
 * @Description: com.example.douyin_publish.domain.po
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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
    /**
     * 作者
     */
    private String author;
}

