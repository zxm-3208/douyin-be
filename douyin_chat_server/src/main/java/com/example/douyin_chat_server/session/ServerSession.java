package com.example.douyin_chat_server.session;

/**
 * @author : zxm
 * @date: 2024/5/31 - 20:05
 * @Description: com.example.douyin_chat.server.session
 * @version: 1.0
 */
public interface ServerSession {
    /**
     * 发送数据
     */
    void writeAndFlush(Object pkg);

    /**
     * 获取sessionId
     */
    String getSessionId();

    /**
     * 是否合法的
     */
    boolean isValid();

    /**
     * 获取用户id
     */
    String getUserId();
}
