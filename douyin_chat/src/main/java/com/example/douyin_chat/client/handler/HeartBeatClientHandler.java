package com.example.douyin_chat.client.handler;

import com.example.douyin_chat.client.builder.HeartBeatMsgBuilder;
import com.example.douyin_chat.client.client.ClientSession;
import com.example.douyin_chat.entity.ChatUserDTO;
import com.example.douyin_chat.protocol.bean.ProtoMsgOuterClass;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author : zxm
 * @date: 2024/6/7 - 13:12
 * @Description: com.example.douyin_chat.client.handler
 * @version: 1.0
 */
@Slf4j
@ChannelHandler.Sharable
@Service
public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter {

    // 心跳的时间间隔, 单位为s
    private static final int HEARTBEAT_INTERVAL = 50;

    // 在通道被激活时，开始发送心跳
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ClientSession session = ClientSession.getSessoin(ctx);
        ChatUserDTO user = session.getUser();
        HeartBeatMsgBuilder builder = new HeartBeatMsgBuilder(user, session);
        ProtoMsgOuterClass.ProtoMsg.Message message = builder.buildMsg();
        // 发送心跳
        heartBeat(ctx, message);
    }

    // 使用定时器，发送心跳报文
    public void heartBeat(ChannelHandlerContext ctx, ProtoMsgOuterClass.ProtoMsg.Message heartbeatMsg){
        ctx.executor().schedule(()->{
            if(ctx.channel().isActive()){
                ctx.writeAndFlush(heartbeatMsg);
                heartBeat(ctx, heartbeatMsg);
            }
        }, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    // 接收到服务器的心跳回写

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 判断消息实例
        if(null == msg || !(msg instanceof ProtoMsgOuterClass.ProtoMsg.Message)){
            super.channelRead(ctx, msg);
            return;
        }
        // 判断类型
        ProtoMsgOuterClass.ProtoMsg.Message pkg = (ProtoMsgOuterClass.ProtoMsg.Message) msg;
        ProtoMsgOuterClass.ProtoMsg.HeadType headType = pkg.getType();
        if(headType.equals(ProtoMsgOuterClass.ProtoMsg.HeadType.HEART_BEAT)){
            log.info(" 收到回写的 HEART_BEAT  消息 from server");
            return;
        }else {
            super.channelRead(ctx, msg);
        }
    }
}
