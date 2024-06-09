package com.example.douyin_chat_gate.service;

import com.example.douyin_chat_commons.domain.vo.ChatUserVo;
import com.example.douyin_chat_commons.domain.vo.SendChat;
import com.example.douyin_commons.core.domain.BaseResponse;
import org.springframework.stereotype.Service;

/**
 * @author : zxm
 * @date: 2024/6/9 - 17:52
 * @Description: com.example.douyin_chat_gate.service
 * @version: 1.0
 */
public interface GateService {

    BaseResponse login(ChatUserVo chatUser);

    void sendChat(SendChat sendChat);

}
