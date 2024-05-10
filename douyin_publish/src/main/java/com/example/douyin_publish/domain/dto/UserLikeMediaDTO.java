package com.example.douyin_publish.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author : zxm
 * @date: 2024/5/10 - 15:01
 * @Description: com.example.douyin_publish.domain.dto
 * @version: 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserLikeMediaDTO {
    private String mediaId;
    private Date updateTime;
    private String coverUrl;
}
