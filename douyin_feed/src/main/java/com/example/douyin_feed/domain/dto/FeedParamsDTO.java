package com.example.douyin_feed.domain.dto;

import com.example.douyin_feed.domain.po.DyMedia;
import com.example.douyin_feed.domain.po.DyPublish;
import lombok.Data;

/**
 * @author : zxm
 * @date: 2024/4/9 - 12:12
 * @Description: com.example.douyin_publish.domain.dto
 * @version: 1.0
 */
@Data
public class FeedParamsDTO {

    public FeedParamsDTO(DyMedia dyMedia, DyPublish dyPublish) {
        this.dyMedia = dyMedia;
        this.dyPublish = dyPublish;
    }

    private DyMedia dyMedia;

    private DyPublish dyPublish;

    /**
     * 文件content-type
     */
    private String contentType;

    /**
     *  文件大小
     */
    private Long fileSize;

    /**
     *  总分片数量
     */
    private int chunks;

    /**
     *  当前为第几块分块
     */
    private int chunk;

}
