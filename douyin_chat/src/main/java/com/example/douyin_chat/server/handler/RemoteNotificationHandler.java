package com.example.douyin_chat.server.handler;

import com.example.douyin_chat.constants.ServerConstants;
import com.example.douyin_chat.entity.ImNode;
import com.example.douyin_chat.protocol.bean.ProtoMsgOuterClass;
import com.example.douyin_chat.server.session.LocalSession;
import com.example.douyin_chat.server.session.service.SessionManger;
import com.example.douyin_chat.util.JsonUtil;
import com.example.douyin_chat.util.Notification;
import com.google.gson.reflect.TypeToken;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.Channel;
import java.text.Normalizer;

/**
 * @author : zxm
 * @date: 2024/6/6 - 11:26
 * @Description: com.example.douyin_chat.server.handler
 * @version: 1.0
 */
@Slf4j
@Service
@ChannelHandler.Sharable
public class RemoteNotificationHandler extends ChannelInboundHandlerAdapter {

    /**
     * 收到消息
     */
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        if (null == msg || !(msg instanceof ProtoMsgOuterClass.ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }
        ProtoMsgOuterClass.ProtoMsg.Message pkg = (ProtoMsgOuterClass.ProtoMsg.Message) msg;

        // 取得请求类型，如果不是通知类型，直接跳过
        ProtoMsgOuterClass.ProtoMsg.HeadType headType = pkg.getType();
        if(!headType.equals(ProtoMsgOuterClass.ProtoMsg.HeadType.MESSAGE_NOTIFICATION)){
            super.channelRead(ctx, msg);
            return;
        }

        // 处理消息内容
        ProtoMsgOuterClass.ProtoMsg.MessageNotification notificationPkg = pkg.getNotification();
        String json = notificationPkg.getJson();

        log.info("收到通知, json={}", json);
        Notification<Notification.ContentWrapper> notification = JsonUtil.jsonToPojo(json, new TypeToken<Notification<Notification.ContentWrapper>>(){}.getType());
        // 下线的通知
        if(notification.getType() == Notification.SESSION_OFF){
            String sid = notification.getWrapperContent();
            log.info("收到用户下线的通知,sid={}", sid);
            SessionManger.inst().removeRemoteSession(sid);
        }
        // 上线的通知
        if(notification.getType() == Notification.SESSION_ON){
            String sid = notification.getWrapperContent();
            log.info("收到用户上线的通知, sid={}", sid);
        }
        // 节点的链接成功
        if(notification.getType() == Notification.CONNECT_FINISHED){
            Notification<ImNode> nodeInfo = JsonUtil.jsonToPojo(json, new TypeToken<Notification<ImNode>>(){}.getType());

            log.info("收到分布式节点连接成功通知, node={}", json);

            ctx.pipeline().remove("login");
            ctx.channel().attr(ServerConstants.CHANNEL_NAME).set(JsonUtil.pojoToJson(nodeInfo));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{
        LocalSession session = LocalSession.getSession(ctx);
        if(null != session){
            session.unbind();
        }
    }

}
