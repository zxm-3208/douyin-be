package com.example.douyin_chat.protocol.protoBuilder;

import com.example.douyin_chat.protocol.bean.ProtoMsgOuterClass;
import com.example.douyin_chat.protocol.constant.ProtoInstant;
import org.springframework.stereotype.Service;

/**
 * @author : zxm
 * @date: 2024/6/5 - 15:48
 * @Description: com.example.douyin_chat.protocol.protoBuilder
 * @version: 1.0
 */
@Service
public class LoginResponceBuilder {

    /**
     * 登录应答， 应答消息protobuf
     */
    public ProtoMsgOuterClass.ProtoMsg.Message loginResponce(ProtoInstant.ResultCodeEnum en, long seqId, String sessionId){
        ProtoMsgOuterClass.ProtoMsg.Message.Builder mb = ProtoMsgOuterClass.ProtoMsg.Message.newBuilder()
                .setType(ProtoMsgOuterClass.ProtoMsg.HeadType.LOGIN_RESPONSE)       // 消息类型
                .setSequence(seqId)     // 序列号
                .setSessionId(sessionId);           //会话ID

        ProtoMsgOuterClass.ProtoMsg.LoginResponse.Builder b = ProtoMsgOuterClass.ProtoMsg.LoginResponse.newBuilder()
                .setCode(en.getCode())
                .setInfo(en.getDesc())
                .setExpose(1);

        mb.setLoginResponse(b.build());
        return mb.build();

    }

}
