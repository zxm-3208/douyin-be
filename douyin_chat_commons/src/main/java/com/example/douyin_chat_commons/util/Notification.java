package com.example.douyin_chat_commons.util;

import lombok.Data;

/**
 * @author : zxm
 * @date: 2024/6/2 - 12:49
 * @Description: com.example.douyin_chat.util
 * @version: 1.0
 */
@Data
public class Notification <T>{
    public static final int SESSION_ON = 10;    // 上线的通知
    public static final int SESSION_OFF = 20;   // 下线的通知
    public static final int CONNECT_FINISHED = 30;  // 节点的链接成功
    private int type;
    private T data;

    public Notification(){

    }

    public Notification(T t){
        data = t;
    }

    // 封装类
    public static Notification<ContentWrapper> wrapContent(String content){
        ContentWrapper wrapper = new ContentWrapper();
        wrapper.setContent(content);
        return new Notification<ContentWrapper>(wrapper);
    }

    @Data
    public static class ContentWrapper{
        String content;
    }

    public String getWrapperContent(){
        if(data instanceof ContentWrapper){
            return ((ContentWrapper) data).getContent();
        }
        return null;
    }


}
