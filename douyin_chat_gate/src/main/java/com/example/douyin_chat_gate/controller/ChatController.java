package com.example.douyin_chat_gate.controller;

import com.example.douyin_chat_commons.domain.vo.ChatUserVo;
import com.example.douyin_chat_commons.domain.vo.SendChat;
import com.example.douyin_chat_commons.util.ThreadUtil;
import com.example.douyin_chat_gate.service.GateService;
import com.example.douyin_commons.core.domain.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

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

    static ThreadPoolExecutor ioPool = null;

    static{
        ioPool = ThreadUtil.getIoIntenseTargetThreadPool();
    }

    @PostMapping("/login")
    public BaseResponse login(@RequestBody ChatUserVo chatUser){
        return gateService.login(chatUser);
    }

    @PostMapping("/login2")
    public BaseResponse login2(@RequestBody ChatUserVo chatUser){
        return gateService.login2(chatUser);
    }

    @PostMapping("sendChat")
    public void sendChat(@RequestBody SendChat sendChat){
        log.info("发送消息：{}", sendChat);
        gateService.sendChat(sendChat);
    }
}
