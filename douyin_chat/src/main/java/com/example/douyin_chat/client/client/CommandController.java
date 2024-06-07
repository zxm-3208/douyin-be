package com.example.douyin_chat.client.client;

import com.example.douyin_chat.cocurrent.FutureTaskScheduler;
import com.example.douyin_chat.entity.ChatUserDTO;
import com.example.douyin_commons.core.domain.UserDTO;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author : zxm
 * @date: 2024/6/7 - 12:15
 * @Description: com.example.douyin_chat.client.client
 * @version: 1.0
 */
@Slf4j
@Data
@Service
public class CommandController {

    private int reConnectCount = 0;

    // 聊天命令收集类
    @Autowired
    ChatConsoleCommand chatConsoleCommand;

    private boolean connectFlag = false;
    private ChatUserDTO user;

    @Autowired
    private NettyClient nettyClient;

    public synchronized void notifyCommandThread()
    {
        //唤醒，命令收集程
        this.notify();
    }


    public void startConnectServer()
    {

        FutureTaskScheduler.add(() ->
        {
            nettyClient.setConnectedListener(connectedListener);
            nettyClient.doConnect();
        });
    }
}
