package com.example.douyin_chat.server.session;

import com.example.douyin_chat.constants.ServerConstants;
import com.example.douyin_chat.entity.ChatUserDTO;
import com.example.douyin_chat.server.session.dao.service.SessionManger;
import com.example.douyin_chat.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;


/**
 * @author : zxm
 * @date: 2024/5/31 - 20:05
 * @Description: com.example.douyin_chat.server.session
 * @version: 1.0
 */
@Data
@Slf4j
public class LocalSession implements ServerSession{

    // AttributeKey 原子变量，线程安全，上下文关联变量
    public static final AttributeKey<String> KEY_USER_ID = AttributeKey.valueOf("key_user_id");
    public static final AttributeKey<LocalSession> SESSION_KEY = AttributeKey.valueOf("SESSION_KEY");

    // 通道
    private Channel channel;
    // 用户
    private ChatUserDTO user;
    // Session唯一标识
    private final String sessionId;
    // 登录状态
    private boolean isLogin = false;

    public LocalSession(Channel channel) {
        this.channel = channel;
        this.sessionId = buildNewSessionId();
    }

    // 反向导航
    public static LocalSession getSession(ChannelHandlerContext ctx){
        return ctx.channel().attr(LocalSession.SESSION_KEY).get();
    }

    // 和channel 实现双向绑定
    public LocalSession bind(){
        log.info("LocalSession 绑定会话{}" , channel.remoteAddress());
        channel.attr(LocalSession.SESSION_KEY).set(this);
        channel.attr(ServerConstants.CHANNEL_NAME).set(JsonUtil.pojoToJson(user));
        isLogin = true;
        return this;
    }

    public LocalSession unbind(){
        isLogin = false;
        SessionManger.inst().removeSession(getSessionId());
        this.close();
        return this;
    }

    private String buildNewSessionId() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-","");
    }


    @Override
    public void writeAndFlush(Object pkg) {

    }

    @Override
    public String getSessionId() {
        return null;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public String getUserId() {
        return null;
    }
}
