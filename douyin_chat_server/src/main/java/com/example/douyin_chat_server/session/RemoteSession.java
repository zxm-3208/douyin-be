package com.example.douyin_chat_server.session;


import com.example.douyin_chat_server.distributed.PeerSender;
import com.example.douyin_chat_server.distributed.WorkerRouter;
import com.example.douyin_chat_commons.entity.ImNode;
import com.example.douyin_chat_server.session.entity.SessionCache;
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

    // 构造函数
    public RemoteSession(SessionCache cache){
        this.cache = cache;
    }

    /**
     * 通过远程节点，转发
     */
    @Override
    public void writeAndFlush(Object pkg) {
        ImNode imNode = cache.getImNode();
        long nodeId = imNode.getId();
        // 获取转发的sender
        PeerSender sender = WorkerRouter.getInstance().route(nodeId);

        if(null!=sender) {
            sender.writeAndFlush(pkg);
        }
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
