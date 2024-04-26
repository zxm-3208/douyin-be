package com.example.douyin_commons.core.domain;

import lombok.Data;

/**
 * @author : zxm
 * @date: 2024/4/26 - 14:18
 * @Description: com.example.douyin_publish.domain.dto
 * @version: 1.0
 */
@Data
public class CoverPublistDTO {
    public CoverPublistDTO(String mediaId, String coverUrl) {
        this.mediaId = mediaId;
        this.coverUrl = coverUrl;
    }

    public CoverPublistDTO() {
    }

    private String mediaId;
    private String coverUrl;
}
