package com.example.douyin_chat_server.handler;

import com.example.douyin_chat_commons.cocurrent.FutureTaskScheduler;
import com.example.douyin_chat_commons.protocol.bean.ProtoMsgOuterClass;
import com.example.douyin_chat_server.process.ChatRedirectProcesser;
import com.example.douyin_chat_server.session.LocalSession;
import com.example.douyin_chat_server.session.ServerSession;
import com.example.douyin_chat_server.session.service.SessionManger;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : zxm
 * @date: 2024/6/6 - 14:17
 * @Description: com.example.douyin_chat.server.handler
 * @version: 1.0
 */
@Slf4j
@Service
@ChannelHandler.Sharable
public class ChatRedirectHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    ChatRedirectProcesser redirectProcesser;

    @Autowired
    SessionManger sessionManger;

    /**
     * 收到消息
     */
    public void channelRead(ChannelHandlerContext ctx,Object msg) throws Exception{
        // 判断消息实例
        if(null == msg || !(msg instanceof ProtoMsgOuterClass.ProtoMsg.Message)){
            super.channelRead(ctx, msg);
            return;
        }
        // 判断消息类型
        ProtoMsgOuterClass.ProtoMsg.Message pkg = (ProtoMsgOuterClass.ProtoMsg.Message) msg;
        ProtoMsgOuterClass.ProtoMsg.HeadType headType = pkg.getType();
        if(!headType.equals(redirectProcesser.op())){
            super.channelRead(ctx, msg);
            return;
        }

        // 异步处理转发逻辑
        FutureTaskScheduler.add(()->{
            // 判断是否登录，如果登陆了，则为用户消息
            LocalSession session = LocalSession.getSession(ctx);
            if(null!=session && session.isLogin()){
                redirectProcesser.action(session, pkg);
                return;
            }

            // 没有登录，则为中转消息
            ProtoMsgOuterClass.ProtoMsg.MessageRequest request = pkg.getMessageRequest();
            List<ServerSession> toSessions = SessionManger.inst().getSessionsBy(request.getTo());
            toSessions.forEach((serverSession -> {
                if(serverSession instanceof LocalSession){
                    // 将im消息发送给接收方
                    serverSession.writeAndFlush(pkg);
                }
            }));
        });
    }

    @Override
    // 当一个 Channel（网络连接）变得不可用或被关闭时，Netty 会触发这个事件
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{
        LocalSession session = ctx.channel().attr(LocalSession.SESSION_KEY).get();
        if (null != session && session.isValid())
        {
            session.close();
            sessionManger.removeSession(session.getSessionId());
        }
    }

}
