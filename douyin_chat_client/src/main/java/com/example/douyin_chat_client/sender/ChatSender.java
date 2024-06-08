package com.example.douyin_chat_client.sender;


import com.example.douyin_chat_client.builder.ChatMsgBuilder;
import com.example.douyin_chat_commons.entity.ChatMsg;
import com.example.douyin_chat_commons.protocol.bean.ProtoMsgOuterClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author : zxm
 * @date: 2024/6/8 - 11:42
 * @Description: com.example.douyin_chat.client.sender
 * @version: 1.0
 */
@Slf4j
@Service
public class ChatSender extends BaseSender{

    public void sendChatMsg(String toUid, String content){
        ChatMsg chatMsg = new ChatMsg(getUser());
        chatMsg.setContent(content);
        chatMsg.setMsgType(ChatMsg.MSGTYPE.TEXT);
        chatMsg.setTo(toUid);
        chatMsg.setMsgId(System.currentTimeMillis());
        ProtoMsgOuterClass.ProtoMsg.Message message = ChatMsgBuilder.buildChatMsg(chatMsg, getUser(), getSession());
        super.sendMsg(message);
    }

    @Override
    protected void sendSucced(ProtoMsgOuterClass.ProtoMsg.Message message)
    {

        log.info("单聊发送成功:{}->{}", message.getMessageRequest().getContent(), message.getMessageRequest().getTo());
    }

    @Override
    protected void sendException(ProtoMsgOuterClass.ProtoMsg.Message message)
    {
        log.info("单聊发送异常:{}->{}", message.getMessageRequest().getContent(),message.getMessageRequest().getTo());
    }

    @Override
    protected void sendfailed(ProtoMsgOuterClass.ProtoMsg.Message message)
    {
        log.info("单聊发送失败:{}->{}", message.getMessageRequest().getContent(), message.getMessageRequest().getTo());
    }

}
