package com.example.douyin_chat_client.client;


import com.example.douyin_chat_commons.entity.ChatUserDTO;
import com.example.douyin_chat_commons.protocol.bean.ProtoMsgOuterClass;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : zxm
 * @date: 2024/6/7 - 11:52
 * @Description: 实现客户端 Session会话
 * @version: 1.0
 */
@Slf4j
@Data
public class ClientSession {
    public static final AttributeKey<ClientSession> SESSION_KEY = AttributeKey.valueOf("SESSION_KEY");

    /**
     * 用户实现客户端会话管理的核心
     */
    private Channel channel;
    private ChatUserDTO user;

    /**
     * 保存登录后的服务端seesionId
     */
    private String sessionId;

    private boolean isConnected = false;
    private boolean isLogin = false;

    /**
     * session 中存储的session 变量属性值
     */
    private Map<String, Object> map = new HashMap<>();

    // 绑定通道
    public ClientSession(Channel channel){
        this.channel = channel;
        this.sessionId = String.valueOf(-1);
        channel.attr(ClientSession.SESSION_KEY).set(this);
    }

    // 登录成功之后，设置sessionId
    public static void loginSuccess(ChannelHandlerContext ctx, ProtoMsgOuterClass.ProtoMsg.Message pkg){
        Channel channel = ctx.channel();
        ClientSession session = channel.attr(ClientSession.SESSION_KEY).get();
        session.setSessionId(pkg.getSessionId());
        session.setLogin(true);
        log.info("登录成功");
    }

    // 获取channel
    public static ClientSession getSessoin(ChannelHandlerContext ctx){
        Channel channel = ctx.channel();
        ClientSession session = channel.attr(ClientSession.SESSION_KEY).get();
        return session;
    }

    public String getRemoteAddress(){
        return channel.remoteAddress().toString();
    }

    // 写protobug 数据帧
    public ChannelFuture writeAndFlush(Object pkg){
        ChannelFuture f = channel.writeAndFlush(pkg);
        return f;
    }

    public void writeAndClose(Object pkg)
    {
        ChannelFuture future = channel.writeAndFlush(pkg);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    // 关闭通道
    public void close(){
        isConnected = false;
        ChannelFuture future = channel.close();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if(future.isSuccess()){
                    log.error("连接顺利断开");
                }
            }
        });
    }

}
