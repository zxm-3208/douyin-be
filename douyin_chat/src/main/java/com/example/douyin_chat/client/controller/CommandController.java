package com.example.douyin_chat.client.controller;

import com.example.douyin_chat.client.client.ClientSession;
import com.example.douyin_chat.client.client.NettyClient;
import com.example.douyin_chat.cocurrent.FutureTaskScheduler;
import com.example.douyin_chat.entity.ChatUserDTO;
import com.example.douyin_commons.core.domain.UserDTO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
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


    private boolean connectFlag = false;
    private ChatUserDTO user;
    private Channel channel;
    private ClientSession session;

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

    GenericFutureListener<ChannelFuture> closeListener = (ChannelFuture f) ->{
        log.info("{}：连接已经断开", new Date());
        channel = f.channel();

        ClientSession session = channel.attr(ClientSession.SESSION_KEY).get();
        session.close();

        //唤醒用户线程
        notifyCommandThread();
    };

    GenericFutureListener<ChannelFuture> connectedListener = (ChannelFuture f)->{
        final EventLoop eventLoop = f.channel().eventLoop();
        if(!f.isSuccess() && ++reConnectCount<3){
            log.info("连接失败！在10秒之后尝试第{}次重连",reConnectCount);
            eventLoop.schedule(()->nettyClient.doConnect(), 10, TimeUnit.SECONDS);
            connectFlag = false;
        }else if(f.isSuccess()){
            connectFlag = true;
            log.info("IM服务器连接成功");
            channel = f.channel();

            // 创建会话
            session = new ClientSession(channel);
            session.setConnected(true);
            channel.closeFuture().addListener(closeListener);

            // 唤醒用户线程
            notifyCommandThread();
        }else{
            log.info("IM服务器多次连接失败！");
            connectFlag = false;
            // 唤醒用户线程
            notifyCommandThread();
        }
    };


}
