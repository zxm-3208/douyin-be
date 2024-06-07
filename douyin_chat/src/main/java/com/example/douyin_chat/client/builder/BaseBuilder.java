package com.example.douyin_chat.client.builder;

import com.example.douyin_chat.client.client.ClientSession;
import com.example.douyin_chat.protocol.bean.ProtoMsgOuterClass;

/**
 * @author : zxm
 * @date: 2024/6/7 - 12:34
 * @Description: com.example.douyin_chat.client.builder
 * @version: 1.0
 */
public class BaseBuilder {
    protected ProtoMsgOuterClass.ProtoMsg.HeadType type;
    private long seqId;
    private ClientSession session;

    public BaseBuilder(ProtoMsgOuterClass.ProtoMsg.HeadType type, ClientSession session){
        this.type = type;
        this.session = session;
    }

    /**
     * 构建消息， 基础部分
     */
    public ProtoMsgOuterClass.ProtoMsg.Message buildCommon(long seqId){
        this.seqId = seqId;
        ProtoMsgOuterClass.ProtoMsg.Message.Builder mb = ProtoMsgOuterClass.ProtoMsg.Message
                .newBuilder()
                .setType(type)
                .setSessionId(session.getSessionId())
                .setSequence(seqId);
        return mb.buildPartial();
    }
}
