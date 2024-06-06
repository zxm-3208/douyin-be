package com.example.douyin_chat.server.handler;

import com.alibaba.nacos.client.auth.impl.process.LoginProcessor;
import com.example.douyin_chat.cocurrent.CallbackTask;
import com.example.douyin_chat.cocurrent.CallbackTaskScheduler;
import com.example.douyin_chat.protocol.bean.ProtoMsgOuterClass;
import com.example.douyin_chat.protocol.constant.ProtoInstant;
import com.example.douyin_chat.server.process.LoginProcesser;
import com.example.douyin_chat.server.session.LocalSession;
import com.example.douyin_chat.server.session.service.SessionManger;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author : zxm
 * @date: 2024/6/5 - 15:37
 * @Description: com.example.douyin_chat.server.handler
 * @version: 1.0
 */
@Slf4j
@Data
@ChannelHandler.Sharable
@Service
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
                return loginProcesser.action(session, pkg);     // 用户验证，绑定，通知
            }

            // 异步任务返回
            @Override
            public void onBack(Boolean r) {
                if(r){
                    log.info("登录成功:{}", session.getUser());
                    // 异步处理的时候如果添加相同名称的处理器会报错
                    ctx.pipeline().addAfter("login", "heartBeat", new HeartBeatServerHandler());    // 在login处理器之后加一个心跳处理器
                    ctx.pipeline().remove("login");     // 登录成功删除login处理器
                }else{
                    SessionManger.inst().closeSession(ctx);
                    log.info("登录失败:{}", session.getUser());
                }
            }

            // 异步任务异常
            @Override
            public void onException(Throwable t) {
                t.printStackTrace();
                log.info("登录失败:{}", session.getUser());
                SessionManger.inst().closeSession(ctx);
            }
        });

    }

}
