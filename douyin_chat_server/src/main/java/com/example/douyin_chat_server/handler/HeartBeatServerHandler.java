package com.example.douyin_chat_server.handler;

import com.example.douyin_chat_commons.cocurrent.FutureTaskScheduler;
import com.example.douyin_chat_commons.constants.ServerConstants;
import com.example.douyin_chat_commons.protocol.bean.ProtoMsgOuterClass;
import com.example.douyin_chat_server.session.service.SessionManger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author : zxm
 * @date: 2024/6/6 - 10:57
 * @Description: com.example.douyin_chat.server.handler
 * @version: 1.0
 */
@Slf4j
public class HeartBeatServerHandler extends IdleStateHandler {

    public static final int READ_IDLE_GAP = 1500;

    public HeartBeatServerHandler(){
        // 读指定时间内如果没有读取到数据， 触发ReaderIdleEvent; 写禁用; 读写禁用
        super(READ_IDLE_GAP, 0, 0, TimeUnit.SECONDS);
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        // 判断消息实例
        if(null == msg || !(msg instanceof ProtoMsgOuterClass.ProtoMsg.Message)){
            super.channelRead(ctx, msg);
            return;
        }

        ProtoMsgOuterClass.ProtoMsg.Message pkg = (ProtoMsgOuterClass.ProtoMsg.Message) msg;
        // 判断消息类型
        ProtoMsgOuterClass.ProtoMsg.HeadType headType = pkg.getType();
        if(headType.equals(ProtoMsgOuterClass.ProtoMsg.HeadType.HEART_BEAT)){
            // 异步处理，将心跳包，直接回复给客户端
            FutureTaskScheduler.add(()->{
                if(ctx.channel().isActive()){
                    ctx.writeAndFlush(msg);
                }
            });
        }
        super.channelRead(ctx, msg);
    }

    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception{
        log.info("{}秒内未读到数据, 关闭连接{}", READ_IDLE_GAP, ctx.channel().attr(ServerConstants.CHANNEL_NAME).get());
        SessionManger.inst().closeSession(ctx);
    }

}
