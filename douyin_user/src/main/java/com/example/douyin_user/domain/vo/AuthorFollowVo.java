package com.example.douyin_user.domain.vo;

import lombok.Data;

/**
 * @author : zxm
 * @date: 2024/5/12 - 13:10
 * @Description: com.example.douyin_user.domain.vo
 * @version: 1.0
 */
@Data
public class AuthorFollowVo {
    private String userId;
    private String authorId;
    private String isFollow;
}
