package com.example.douyin_chat.server.session;

import com.example.douyin_chat.entity.ImNode;
import com.example.douyin_chat.server.session.entity.SessionCache;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author : zxm
 * @date: 2024/5/31 - 20:05
 * @Description: com.example.douyin_chat.server.session
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@Builder
public class RemoteSession implements ServerSession, Serializable {

    @Serial
    private static final long serialVersionUID = 3608763818357282303L;

    SessionCache cache;

    private boolean valid = true;

    public RemoteSession(SessionCache cache){
        this.cache = cache;
    }

    /**
     * TODO: 通过远程节点，转发
     */
    @Override
    public void writeAndFlush(Object pkg) {

    }

    @Override
    public String getSessionId() {
        return cache.getSessionId();
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public String getUserId() {
        return cache.getUserId();
    }
}
