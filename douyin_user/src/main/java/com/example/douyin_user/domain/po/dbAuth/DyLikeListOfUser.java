package com.example.douyin_user.domain.po.dbAuth;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : zxm
 * @date: 2024/5/9 - 14:57
 * @Description: com.example.douyin_user.domain.po.dbAuth
 * @version: 1.0
 */
@Data
public class DyLikeListOfUser implements Serializable {
    private static final long serialVersionUID = -44388768307976415L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 外键，视频ID
     */
    private String mediaid;
    /**
     * 外键，用户ID
     */
    private String userid;
}