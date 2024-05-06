package com.example.douyin_user.domain.po;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author : zxm
 * @date: 2024/5/6 - 14:28
 * @Description: com.example.douyin_user.domain.po
 * @version: 1.0
 */
@Data
public class DyAttention implements Serializable {
    @Serial
    private static final long serialVersionUID = 3725154253047030600L;

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
    private String attentionid;
}
