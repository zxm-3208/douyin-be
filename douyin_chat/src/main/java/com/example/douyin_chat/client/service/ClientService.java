package com.example.douyin_chat.client.service;

import com.example.douyin_chat.client.domain.vo.ChatUser;
import com.example.douyin_commons.core.domain.BaseResponse;

/**
 * @author : zxm
 * @date: 2024/6/7 - 17:07
 * @Description: com.example.douyin_chat.client.service
 * @version: 1.0
 */
public interface ClientService {


    BaseResponse startCommandThread(ChatUser chatUser);
}
