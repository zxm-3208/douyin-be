package com.example.douyin_user.domain.po;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author : zxm
 * @date: 2024/5/6 - 14:27
 * @Description: com.example.douyin_user.domain.po
 * @version: 1.0
 */
@Data
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
    private String userid;
    /**
     * 外键
     */
    private String followerid;
}
