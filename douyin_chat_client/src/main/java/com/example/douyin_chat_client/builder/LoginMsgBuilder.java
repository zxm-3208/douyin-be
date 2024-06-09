package com.example.douyin_chat_client.builder;


import com.example.douyin_chat_client.client.ClientSession;
import com.example.douyin_chat_commons.domain.DTO.ChatUserDTO;
import com.example.douyin_chat_commons.protocol.bean.ProtoMsgOuterClass;

/**
 * @author : zxm
 * @date: 2024/6/7 - 12:34
 * @Description: 登录消息Builder
 * @version: 1.0
 */
public class LoginMsgBuilder extends BaseBuilder {

    private final ChatUserDTO user;

    public LoginMsgBuilder(ChatUserDTO user, ClientSession session){
        super(ProtoMsgOuterClass.ProtoMsg.HeadType.LOGIN_REQUEST, session);
        this.user = user;
    }

    public ProtoMsgOuterClass.ProtoMsg.Message build(){
        ProtoMsgOuterClass.ProtoMsg.Message message = buildCommon(-1);
        ProtoMsgOuterClass.ProtoMsg.LoginRequest.Builder lb = ProtoMsgOuterClass.ProtoMsg.LoginRequest.newBuilder()
                .setDeviceId(user.getDevId())
                .setPlatform(user.getPlatform().ordinal())
                .setToken(user.getToken())
                .setUid(user.getUserId());
        return message.toBuilder().setLoginRequest(lb).build();
    }

    public static ProtoMsgOuterClass.ProtoMsg.Message buildLoginMsg(ChatUserDTO user, ClientSession session){
         LoginMsgBuilder builder = new LoginMsgBuilder(user, session);
         return builder.build();
    }

}
