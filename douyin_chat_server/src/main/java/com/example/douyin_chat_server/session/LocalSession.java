package com.example.douyin_chat_server.session;


import com.example.douyin_chat_commons.constants.ServerConstants;
import com.example.douyin_chat_commons.entity.ChatUserDTO;
import com.example.douyin_chat_commons.util.JsonUtil;
import com.example.douyin_chat_server.session.service.SessionManger;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
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
    // session 中存储的变量属性值
    private Map<String, Object> map = new HashMap<>();

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

    public synchronized void set(String key, Object value){
        map.put(key, value);
    }

    public synchronized <T> T get(String key){
        return (T) map.get(key);
    }

    public boolean isValid(){
        return getUser()!=null? true:false;
    }

    /**
     * 当系统水位过高时，系统应不继续发送消息，防止发送队列积压
     * 写protobuf数据帧
     */
    @Override
    public void writeAndFlush(Object pkg) {
        if(channel.isWritable()){   // 低水位
            channel.writeAndFlush(pkg);         // 线程安全
        } else{     // 高水位
            log.debug("通道很忙，消息被暂存了");
            // TODO: 采用消息队列进行流量削峰填谷
        }
    }

    // 写Protobuf数据帧
    public synchronized void writeAndClose(Object pkg){
        channel.writeAndFlush(pkg);
        close();
    }

    // 关闭连接
    public synchronized void close(){
        // 用户下线，通知其他节点
        ChannelFuture future = channel.close();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if(!future.isSuccess()){
                    log.error("CHANNEL_CLOSED error");
                }
                else{
                    log.debug("CHANNEL_CLOSED success");
                }
            }
        });
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }


    @Override
    public String getUserId() {
        return user.getUserId();
    }
}
