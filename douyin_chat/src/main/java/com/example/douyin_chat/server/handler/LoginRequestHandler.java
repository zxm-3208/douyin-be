package com.example.douyin_chat.server.handler;

import com.alibaba.nacos.client.auth.impl.process.LoginProcessor;
import com.example.douyin_chat.cocurrent.CallbackTask;
import com.example.douyin_chat.cocurrent.CallbackTaskScheduler;
import com.example.douyin_chat.protocol.bean.ProtoMsgOuterClass;
import com.example.douyin_chat.protocol.constant.ProtoInstant;
import com.example.douyin_chat.server.process.LoginProcesser;
import com.example.douyin_chat.server.session.LocalSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author : zxm
 * @date: 2024/6/5 - 15:37
 * @Description: com.example.douyin_chat.server.handler
 * @version: 1.0
 */
@Slf4j
@Data
@ChannelHandler.Sharable
public class LoginRequestHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    LoginProcesser loginProcesser;

    /**
     * 收到消息
     */
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(null == msg || !(msg instanceof ProtoMsgOuterClass.ProtoMsg.Message)){
            super.channelRead(ctx, msg);
            return;
        }
        ProtoMsgOuterClass.ProtoMsg.Message pkg = (ProtoMsgOuterClass.ProtoMsg.Message) msg;

        // 取得请求类型
        ProtoMsgOuterClass.ProtoMsg.HeadType headType = pkg.getType();

        if(!headType.equals(loginProcesser.op())){
            super.channelRead(ctx, msg);
            return;
        }

        LocalSession session = new LocalSession(ctx.channel());

        // 异步处理，处理登录的逻辑
        CallbackTaskScheduler.add(new CallbackTask<Boolean>() {

            @Override
            public Boolean execute() throws Exception {
                return null;
            }

            @Override
            public void onBack(Boolean aBoolean) {

            }

            @Override
            public void onException(Throwable t) {

            }
        })

    }

}
