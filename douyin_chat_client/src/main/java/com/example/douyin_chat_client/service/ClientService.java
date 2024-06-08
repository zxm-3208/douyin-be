package com.example.douyin_chat_client.service;


import com.example.douyin_chat_client.domain.vo.ChatUser;
import com.example.douyin_chat_client.domain.vo.SendChat;
import com.example.douyin_commons.core.domain.BaseResponse;

/**
 * @author : zxm
 * @date: 2024/6/7 - 17:07
 * @Description: com.example.douyin_chat.client.service
 * @version: 1.0
 */
public interface ClientService {


    BaseResponse login(ChatUser chatUser);

    public void startConnectServer();

    public void setConnectFlag(Boolean x);

    void sendChat(SendChat sendChat);
}
