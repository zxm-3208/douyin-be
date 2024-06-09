package com.example.douyin_chat_client.service;

import com.example.douyin_chat_commons.domain.vo.ChatUserVo;
import com.example.douyin_chat_commons.domain.vo.SendChat;
import com.example.douyin_commons.core.domain.BaseResponse;

/**
 * @author : zxm
 * @date: 2024/6/9 - 16:29
 * @Description: com.example.douyin_chat_gate.service.impl
 * @version: 1.0
 */
public interface ChatService {
    BaseResponse login(String user);

    void sendChat(SendChat sendChat);
}
