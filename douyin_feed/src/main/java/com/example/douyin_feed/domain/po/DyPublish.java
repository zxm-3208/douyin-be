package com.example.douyin_feed.domain.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : zxm
 * @date: 2024/4/9 - 13:36
 * @Description: com.example.douyin_publish.domain.po
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DyPublish implements Serializable {
    private static final long serialVersionUID = 466767465347550373L;
    /**
     * 主键id
     */
    private String id;
    /**
     * 外键，视频的id
     */
    private String mediaId;
    /**
     * 上传的文件名
     */
    private String fileName;
    /**
     * 视频题目
     */
    private String title;
    /**
     * 上传时间
     */
    private Date uploadTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 作者
     */
    private String author;
    /**
     * 发布状态(-1:发布失败,0:正常,1:违规，2:热点...)
     */
    private String status;

    /**
     *  文件类型（文档、音频、视频）
     */
    private String type;
    /**
     * 封面url
     */
    private String imgUrl;
    /**
     * 视频标签
     */
    private String tag;

    private DyUser dyUser;
    private DyUserLikeMedia dyUserLikeMedia;
}
