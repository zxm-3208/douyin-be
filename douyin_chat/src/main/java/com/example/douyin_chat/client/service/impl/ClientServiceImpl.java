package com.example.douyin_chat.client.service.impl;

import com.example.douyin_chat.client.controller.CommandController;
import com.example.douyin_chat.client.domain.vo.ChatUser;
import com.example.douyin_chat.client.service.ClientService;
import com.example.douyin_commons.core.domain.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author : zxm
 * @date: 2024/6/7 - 17:08
 * @Description: com.example.douyin_chat.client.service.impl
 * @version: 1.0
 */
@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    CommandController commandController;

    @Override
    public BaseResponse startCommandThread(ChatUser chatUser) {
        Thread.currentThread().setName("命令线程");
        if(commandController.isConnectFlag() == false){
            // TODO: 登录

        }


        return null;
    }
}
