package com.example.douyin_feed.domain.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : zxm
 * @date: 2024/5/9 - 14:57
 * @Description: com.example.douyin_user.domain.po.dbAuth
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DyUserLikeMedia implements Serializable {
    private static final long serialVersionUID = -76335406885067508L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 外键，用户ID
     */
    private String userId;
    /**
     * 外键，用户ID
     */
    private String mediaId;
    /**
     * 更新时间
     */
    private Date updateTime;

}
