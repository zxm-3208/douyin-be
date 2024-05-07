package com.example.douyin_commons.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : zxm
 * @date: 2024/5/7 - 11:53
 * @Description: com.example.douyin_commons.core.domain
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String id;
    private String nickName;
    private String icon;

}
