package com.example.douyin_chat_server.process;

import com.example.douyin_chat_commons.protocol.bean.ProtoMsgOuterClass;
import com.example.douyin_chat_server.session.LocalSession;
import com.example.douyin_chat_server.session.ServerSession;
import com.example.douyin_chat_server.session.service.SessionManger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : zxm
 * @date: 2024/6/6 - 14:19
 * @Description: com.example.douyin_chat.server.process
 * @version: 1.0
 */
@Slf4j
@Service
public class ChatRedirectProcesser extends AbstractServerProcesser {

    public static final int RE_DIRECT = 1;

    @Override
    public ProtoMsgOuterClass.ProtoMsg.HeadType op() {
        return ProtoMsgOuterClass.ProtoMsg.HeadType.MESSAGE_REQUEST;
    }

    @Override
    public Boolean action(LocalSession ch, ProtoMsgOuterClass.ProtoMsg.Message proto) {
        // 聊天记录
        ProtoMsgOuterClass.ProtoMsg.MessageRequest messageRequest = proto.getMessageRequest();
        log.info("chatMsg | from="
                + messageRequest.getFrom()
                + " , to =" + messageRequest.getTo()
                + " , MsgType =" + messageRequest.getMsgType()
                + " , content =" + messageRequest.getContent());

        // 获取接收方的chatId
        String to = messageRequest.getTo();
        List<ServerSession> toSessions = SessionManger.inst().getSessionsBy(to);
        if(toSessions == null){
            // 接收方离线
            // TODO: 存到数据库？
            log.info("[" + to + "] 不在线，需要保存为离线消息");
        }else{
            toSessions.forEach((session)->{
                // 将IM消息发送到接收客户端
                session.writeAndFlush(proto);
            });
        }
        return null;
    }
}
