package com.example.douyin_chat_gate.controller;

import com.example.douyin_chat_commons.domain.vo.ChatUserVo;
import com.example.douyin_chat_commons.domain.vo.SendChat;
import com.example.douyin_chat_gate.service.GateService;
import com.example.douyin_commons.core.domain.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author : zxm
 * @date: 2024/6/9 - 16:30
 * @Description: com.example.douyin_chat_gate.controller
 * @version: 1.0
 */
@RestController     //@Controller + @ResponseBody
@RequestMapping("/chat")
@CrossOrigin
@Slf4j
public class ChatController {
    @Autowired
    GateService gateService;

    @PostMapping("/login")
    public BaseResponse login(@RequestBody ChatUserVo chatUser){
        return gateService.login(chatUser);
    }

    @PostMapping("sendChat")
    public void sendChat(@RequestBody SendChat sendChat){
        gateService.sendChat(sendChat);
    }
}
