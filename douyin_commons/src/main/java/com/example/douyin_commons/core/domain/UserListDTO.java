package com.example.douyin_commons.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : zxm
 * @date: 2024/5/15 - 19:33
 * @Description: com.example.douyin_user.domain.dto
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserListDTO {
    private String userId;
    private String icon;
    private String userName;
    private String introduction;

}
