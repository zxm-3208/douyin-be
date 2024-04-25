package com.example.douyin_feed.domain.vo;

import lombok.Data;

/**
 * @author : zxm
 * @date: 2024/4/25 - 17:23
 * @Description: com.example.douyin_feed.domain.vo
 * @version: 1.0
 */
@Data
public class ClickPlayVo {
    private String userId;
    private String[] mediaIdList;
}
