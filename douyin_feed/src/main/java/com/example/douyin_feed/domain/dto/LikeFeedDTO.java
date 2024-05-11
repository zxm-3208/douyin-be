package com.example.douyin_feed.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

import java.util.Date;

/**
 * @author : zxm
 * @date: 2024/5/10 - 19:41
 * @Description: com.example.douyin_feed.domain.dto
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeFeedDTO {
    private String mediaId;
    private Date updateTime;
    private String mediaUrl;
    private String userId;
    private String Title;
}
