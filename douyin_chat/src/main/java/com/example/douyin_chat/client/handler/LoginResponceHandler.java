package com.example.douyin_chat.client.handler;

import com.example.douyin_chat.client.client.ClientSession;
import com.example.douyin_chat.client.controller.CommandController;
import com.example.douyin_chat.protocol.bean.ProtoMsgOuterClass;
import com.example.douyin_chat.protocol.constant.ProtoInstant;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author : zxm
 * @date: 2024/6/7 - 13:11
 * @Description: com.example.douyin_chat.client.handler
 * @version: 1.0
 */
@Slf4j
@ChannelHandler.Sharable
@Service
public class LoginResponceHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    CommandController commandController;
    @Autowired
    HeartBeatClientHandler heartBeatClientHandler;

    /**
     * 业务逻辑处理
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 判断消息实例
        if(null == msg || !(msg instanceof ProtoMsgOuterClass.ProtoMsg.Message)){
            super.channelRead(ctx, msg);
            return;
        }
        // 判断类型
        ProtoMsgOuterClass.ProtoMsg.Message pkg = (ProtoMsgOuterClass.ProtoMsg.Message) msg;
        ProtoMsgOuterClass.ProtoMsg.HeadType headType = ((ProtoMsgOuterClass.ProtoMsg.Message) msg).getType();
        if (!headType.equals(ProtoMsgOuterClass.ProtoMsg.HeadType.LOGIN_RESPONSE))
        {
            super.channelRead(ctx, msg);
            return;
        }
        // 判断返回是否成功
        ProtoMsgOuterClass.ProtoMsg.LoginResponse info = pkg.getLoginResponse();
        ProtoInstant.ResultCodeEnum result = ProtoInstant.ResultCodeEnum.values()[info.getCode()];
        if(!result.equals(ProtoInstant.ResultCodeEnum.SUCCESS)){
            log.info(result.getDesc());
            log.info("step3: 登录Netty 服务节点失败");
        }else{
            ClientSession session = ctx.channel().attr(ClientSession.SESSION_KEY).get();
            session.setSessionId(pkg.getSessionId());
            session.setLogin(true);
            log.info("step3：登录Netty 服务节点成功");
            commandController.notifyCommandThread();;
            ctx.channel().pipeline().addAfter("loginResponseHandler", "heartBeatClientHandler", heartBeatClientHandler);
            heartBeatClientHandler.channelActive(ctx);
            ctx.channel().pipeline().remove("loginResponseHandler");

        }

        super.channelRead(ctx, msg);
    }
}
