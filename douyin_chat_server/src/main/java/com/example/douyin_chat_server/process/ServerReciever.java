package com.example.douyin_chat_server.process;


import com.example.douyin_chat_commons.protocol.bean.ProtoMsgOuterClass;
import com.example.douyin_chat_server.session.LocalSession;

/**
 * @author : zxm
 * @date: 2024/6/5 - 15:41
 * @Description: 操作类
 * @version: 1.0
 */
public interface ServerReciever {

    ProtoMsgOuterClass.ProtoMsg.HeadType op();

    Boolean action(LocalSession ch, ProtoMsgOuterClass.ProtoMsg.Message proto);

}
