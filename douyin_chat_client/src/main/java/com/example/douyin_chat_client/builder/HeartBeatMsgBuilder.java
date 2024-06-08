package com.example.douyin_chat_client.builder;

import com.example.douyin_chat_client.client.ClientSession;
import com.example.douyin_chat_commons.entity.ChatUserDTO;
import com.example.douyin_chat_commons.protocol.bean.ProtoMsgOuterClass;

/**
 * @author : zxm
 * @date: 2024/6/7 - 14:05
 * @Description: com.example.douyin_chat.client.builder
 * @version: 1.0
 */
public class HeartBeatMsgBuilder extends BaseBuilder{
    private final ChatUserDTO user;

    public HeartBeatMsgBuilder(ChatUserDTO user, ClientSession session) {
        super(ProtoMsgOuterClass.ProtoMsg.HeadType.HEART_BEAT, session);
        this.user = user;
    }

    public ProtoMsgOuterClass.ProtoMsg.Message buildMsg() {
        ProtoMsgOuterClass.ProtoMsg.Message message = buildCommon(-1);
        ProtoMsgOuterClass.ProtoMsg.MessageHeartBeat.Builder lb = ProtoMsgOuterClass.ProtoMsg.MessageHeartBeat.newBuilder()
                .setSeq(0)
                .setJson("{\"from\":\"client\"}")
                .setUid(user.getUserId());
        return message.toBuilder().setHeartBeat(lb).build();
    }
}
