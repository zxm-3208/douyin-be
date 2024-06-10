package com.example.douyin_chat_gate.service;

import com.example.douyin_chat_commons.domain.vo.BackVo;
import com.example.douyin_chat_commons.domain.vo.ChatUserVo;
import com.example.douyin_chat_commons.domain.vo.SendChat;
import com.example.douyin_commons.core.domain.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author : zxm
 * @date: 2024/6/9 - 21:28
 * @Description: com.example.douyin_chat_gate.service
 * @version: 1.0
 */
@FeignClient("douyinChatClient")
public interface ChatOpenFeignService {

    @PostMapping("/douyin_chat_client/chat/login")
    BaseResponse login(@RequestBody BackVo backVo);

    @PostMapping("/chat/sendChat")
    void sendChat(@RequestBody SendChat sendChat);
}
