package com.example.douyin_commons.core.domain;

import lombok.Data;

/**
 * @author : zxm
 * @date: 2024/4/26 - 15:43
 * @Description: com.example.douyin_publish.domain.dto
 * @version: 1.0
 */
@Data
public class MediaPublistDTO {
    public MediaPublistDTO(String mediaId, String mediaUrl) {
        this.mediaId = mediaId;
        this.mediaUrl = mediaUrl;
    }

    public MediaPublistDTO(String mediaId, String mediaUrl, String userId, String mediaTitle) {
        this.mediaId = mediaId;
        this.mediaUrl = mediaUrl;
        this.userId = userId;
        this.mediaTitle = mediaTitle;
    }

    public MediaPublistDTO(String mediaId, String mediaUrl, String userId, String mediaTitle, String userName, String userIcon) {
        this.mediaId = mediaId;
        this.mediaUrl = mediaUrl;
        this.userId = userId;
        this.mediaTitle = mediaTitle;
        this.userName = userName;
        this.userIcon =userIcon;
    }

    public MediaPublistDTO() {
    }

    private String mediaId;
    private String mediaUrl;
    private String mediaTitle;
    private String userId;
    private String userName;
    private String userIcon;
}
