package com.example.douyin_user.domain.po.dbAuth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author : zxm
 * @date: 2024/5/6 - 14:27
 * @Description: com.example.douyin_user.domain.po
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DyFollow implements Serializable {
    @Serial
    private static final long serialVersionUID = 8735023915713335848L;
    /**
     * 主键
     */
    private String id;
    /**
     * 外键
     */
    private String userId;
    /**
     * 外键
     */
    private String followerId;
    /**
     * 创建时间
     */
    private Date followCreateTime;

    private DyUser dyUser;

}
