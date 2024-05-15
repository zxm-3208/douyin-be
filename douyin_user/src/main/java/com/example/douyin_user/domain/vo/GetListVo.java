package com.example.douyin_user.domain.vo;

import lombok.Data;

/**
 * @author : zxm
 * @date: 2024/5/15 - 19:23
 * @Description: com.example.douyin_user.domain.vo
 * @version: 1.0
 */
@Data
public class GetListVo {
    private String userId;
    private String lastId;
    private String offset;
}
