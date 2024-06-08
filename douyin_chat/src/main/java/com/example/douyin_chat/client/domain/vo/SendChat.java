package com.example.douyin_chat.client.domain.vo;

import lombok.Data;

/**
 * @author : zxm
 * @date: 2024/6/8 - 12:22
 * @Description: com.example.douyin_chat.client.domain.vo
 * @version: 1.0
 */
@Data
public class SendChat {
    String message;
    String toUserId;
}
