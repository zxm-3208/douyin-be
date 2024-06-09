package com.example.douyin_chat_commons.domain.DTO;


import com.example.douyin_chat_commons.protocol.bean.ProtoMsgOuterClass;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : zxm
 * @date: 2024/5/31 - 20:24
 * @Description: com.example.douyin_chat.entity
 * @version: 1.0
 */
// TODO: 相应数据可以用数据库中读取
@Slf4j
@Data
public class ChatUserDTO {
    String userId;
    String userName;
    String devId;
    String token;
    String nickName = "nickName";
    PLATTYPE platform = PLATTYPE.WINDOWS;
    private String sessionId;

    public enum PLATTYPE
    {
        WINDOWS, MAC, ANDROID, IOS, WEB, OTHER;
    }

    public void setPlatform(int platform)
    {
        PLATTYPE[] values = PLATTYPE.values();
        for (int i = 0; i < values.length; i++)
        {
            if (values[i].ordinal() == platform)        // ordinal用来返回枚举常量的序数
            {
                this.platform = values[i];
            }
        }
    }

    public String toString()
    {
        return "User{" +
                "uid='" + userId + '\'' +
                ", devId='" + devId + '\'' +
                ", token='" + token + '\'' +
                ", nickName='" + nickName + '\'' +
                ", platform=" + platform +
                '}';
    }

    // 根据info内的数据创建用户
    public static ChatUserDTO fromMsg(ProtoMsgOuterClass.ProtoMsg.LoginRequest info){
        ChatUserDTO user = new ChatUserDTO();
        user.setUserId(info.getUid());          // 用户ID
        user.setDevId(info.getDeviceId());      // 设备ID
        user.setToken(info.getToken());         // token
        user.setPlatform(info.getPlatform());   // 设备平台
        log.info("登录中：{}", user.toString());
        return user;
    }
}
