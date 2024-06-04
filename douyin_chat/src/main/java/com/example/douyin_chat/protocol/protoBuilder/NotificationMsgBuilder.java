package com.example.douyin_chat.protocol.protoBuilder;

import com.example.douyin_chat.protocol.bean.ProtoMsgOuterClass;

/**
 * @author : zxm
 * @date: 2024/6/4 - 10:49
 * @Description: com.example.douyin_chat.protocol.protoBuilder
 * @version: 1.0
 */
public class NotificationMsgBuilder {

    public static ProtoMsgOuterClass.ProtoMsg.Message buildNotification(String json){
        ProtoMsgOuterClass.ProtoMsg.Message.Builder mb = ProtoMsgOuterClass.ProtoMsg.Message.newBuilder()
                .setType(ProtoMsgOuterClass.ProtoMsg.HeadType.MESSAGE_NOTIFICATION);        // 设置消息类型
        // 设置应答流水，与请求对应
        ProtoMsgOuterClass.ProtoMsg.MessageNotification.Builder rb =
                ProtoMsgOuterClass.ProtoMsg.MessageNotification.newBuilder()
                .setJson(json);
        mb.setNotification(rb.build());
        return mb.build();

    }


}
