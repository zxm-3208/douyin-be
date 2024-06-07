package com.example.douyin_chat.client.handler;

import com.example.douyin_chat.client.sender.LoginSender;
import com.example.douyin_chat.protocol.bean.ProtoMsgOuterClass;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.SuppressJava6Requirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author : zxm
 * @date: 2024/6/7 - 11:49
 * @Description: com.example.douyin_chat.client.handler
 * @version: 1.0
 */
@ChannelHandler.Sharable
@Slf4j
@Service
public class ChatMsgHandler extends ChannelInboundHandlerAdapter {
    private LoginSender sender;
    public ChatMsgHandler(LoginSender sender){
        this.sender = sender;
    }

    /**
     * 业务逻辑处理
     */
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        // 判断消息实例
        if(null == msg || !(msg instanceof ProtoMsgOuterClass.ProtoMsg.Message)){
            super.channelRead(ctx, msg);
            return;
        }
        // 判断类型
        ProtoMsgOuterClass.ProtoMsg.Message pkg = (ProtoMsgOuterClass.ProtoMsg.Message) msg;
        ProtoMsgOuterClass.ProtoMsg.HeadType headType = pkg.getType();
        if(!headType.equals(ProtoMsgOuterClass.ProtoMsg.HeadType.MESSAGE_REQUEST)){
            super.channelRead(ctx, msg);
            return;
        }

        ProtoMsgOuterClass.ProtoMsg.MessageRequest req = pkg.getMessageRequest();
        String content = req.getContent();
        String uid = req.getFrom();
        log.info("收到消息from uid:{}->{}", uid, content);
    }
}
