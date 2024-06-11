package com.example.douyin_chat_client.sender;


import com.example.douyin_chat_client.builder.LoginMsgBuilder;
import com.example.douyin_chat_commons.protocol.bean.ProtoMsgOuterClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author : zxm
 * @date: 2024/6/7 - 11:50
 * @Description: com.example.douyin_chat.client.sender
 * @version: 1.0
 */
@Slf4j
@Service
public class LoginSender extends BaseSender{
    public void sendLoginMsg(){
        if(!isConnected()){
            log.info("还没有建立连接");
            return;
        }
        log.info("发送登录消息");
        ProtoMsgOuterClass.ProtoMsg.Message message = LoginMsgBuilder.buildLoginMsg(getUser(), getSession());
        super.sendMsg(message);
    }
}
