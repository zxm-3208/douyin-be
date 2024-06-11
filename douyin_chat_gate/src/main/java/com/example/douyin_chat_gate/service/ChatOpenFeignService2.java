package com.example.douyin_chat_gate.service;

import com.example.douyin_chat_commons.domain.vo.BackVo;
import com.example.douyin_chat_commons.domain.vo.SendChat;
import com.example.douyin_commons.core.domain.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author : zxm
 * @date: 2024/6/9 - 21:28
 * @Description: com.example.douyin_chat_gate.service
 * @version: 1.0
 */
@FeignClient("douyinChatClientTwo")
public interface ChatOpenFeignService2 {

    @PostMapping("/douyin_chat_client_two/chat/login")
    BaseResponse login(@RequestBody BackVo backVo);

    @PostMapping("/douyin_chat_client_two/chat/sendChat")
    void sendChat(@RequestBody SendChat sendChat);
}
