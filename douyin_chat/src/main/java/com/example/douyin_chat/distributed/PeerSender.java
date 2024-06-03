package com.example.douyin_chat.distributed;

import com.example.douyin_chat.entity.ChatUserDTO;
import com.example.douyin_chat.entity.ImNode;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.Channel;

/**
 * @author : zxm
 * @date: 2024/6/2 - 19:48
 * @Description: com.example.douyin_chat.distributed
 * @version: 1.0
 */
@Slf4j
@Data
public class PeerSender {

    private int reConnectCount = 0;
    private Channel channel;
    private ImNode rmNode;
    /**
     * 唯一标记
     */
    private boolean connectFlag = false;
    private ChatUserDTO user;

    GenericFutureListener<ChannelFuture> closeListener = (ChannelFuture f) ->{
        log.info("分布式连接已经断开...{}", rmNode.toString());
        channel = null;
        connectFlag = false;
    };

    private GenericFutureListener<ChannelFuture> connectedListener = (ChannelFuture f)->{

    };




}
