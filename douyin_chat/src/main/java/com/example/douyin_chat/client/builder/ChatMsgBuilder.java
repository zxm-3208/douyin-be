package com.example.douyin_chat.client.builder;

import com.example.douyin_chat.client.client.ClientSession;
import com.example.douyin_chat.client.service.ClientService;
import com.example.douyin_chat.entity.ChatMsg;
import com.example.douyin_chat.entity.ChatUserDTO;
import com.example.douyin_chat.protocol.bean.ProtoMsgOuterClass;
import com.example.douyin_commons.core.domain.UserDTO;

/**
 * @author : zxm
 * @date: 2024/6/8 - 11:52
 * @Description: com.example.douyin_chat.client.builder
 * @version: 1.0
 */
public class ChatMsgBuilder extends BaseBuilder{

    private ChatMsg chatMsg;
    private ChatUserDTO user;

    public ChatMsgBuilder(ChatMsg chatMsg, ChatUserDTO user, ClientSession session) {
        super(ProtoMsgOuterClass.ProtoMsg.HeadType.MESSAGE_REQUEST, session);
        this.chatMsg = chatMsg;
        this.user = user;
    }

    public ProtoMsgOuterClass.ProtoMsg.Message build(){
        ProtoMsgOuterClass.ProtoMsg.Message message = buildCommon(-1);
        ProtoMsgOuterClass.ProtoMsg.MessageRequest.Builder cb = ProtoMsgOuterClass.ProtoMsg.MessageRequest.newBuilder();

        chatMsg.fillMsg(cb);
        return message.toBuilder().setMessageRequest(cb).build();

    }

    public static ProtoMsgOuterClass.ProtoMsg.Message buildChatMsg(ChatMsg chatMsg, ChatUserDTO user, ClientSession session)
    {
        ChatMsgBuilder builder = new ChatMsgBuilder(chatMsg, user, session);
        return builder.build();
    }


}
