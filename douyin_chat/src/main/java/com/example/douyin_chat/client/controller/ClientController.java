package com.example.douyin_chat.client.controller;

import com.example.douyin_chat.client.domain.vo.ChatUser;
import com.example.douyin_chat.client.service.ClientService;
import com.example.douyin_commons.core.domain.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author : zxm
 * @date: 2024/6/7 - 16:36
 * @Description: com.example.douyin_chat.client.controller
 * @version: 1.0
 */
@RestController     //@Controller + @ResponseBody
@RequestMapping("/chat")
@CrossOrigin
@Slf4j
public class ClientController {

    @Autowired
    ClientService clientService;

    @PostMapping("/login")
    public BaseResponse startCommandThread(@RequestBody ChatUser chatUser){
        return clientService.startCommandThread(chatUser);
    }

}
