package com.example.douyin_feed.domain.po;

import lombok.Data;

/**
 * @author : zxm
 * @date: 2024/4/26 - 21:05
 * @Description: com.example.douyin_publish.domain.po
 * @version: 1.0
 */
@Data
public class MediaJoinPublish {
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

    private DyPublish dyPublish;

    public MediaJoinPublish() {
    }

    public MediaJoinPublish(String id, String mediaUrl, String status, String md5, String author, DyPublish dyPublish) {
        this.id = id;
        this.mediaUrl = mediaUrl;
        this.status = status;
        this.md5 = md5;
        this.author = author;
        this.dyPublish = dyPublish;
    }
}
