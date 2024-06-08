package com.example.douyin_chat_server.session.entity;


import com.example.douyin_chat_commons.entity.ImNode;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author : zxm
 * @date: 2024/5/31 - 21:59
 * @Description: com.example.douyin_chat.server.session.dao.entity
 * @version: 1.0
 */
@Data           // 只公开getter，对所有属性的setter都封闭
@Builder        // 它提供在设计数据实体时，对外保持private setter，而对属性的赋值采用Builder的方式
public class SessionCache implements Serializable {
    @Serial
    private static final long serialVersionUID = -2150362449057558138L;

    // 用户的ID
    private String userId;
    // seesion id
    private String sessionId;
    // 节点信息
    private ImNode imNode;

    public SessionCache(){
        userId = "";
        sessionId = "";
        imNode = new ImNode("unKnown", 0);
    }

    public SessionCache(
            String sessionId, String userId, ImNode imNode) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.imNode = imNode;
    }
}
