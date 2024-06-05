package com.example.douyin_chat.server.process;

import com.example.douyin_chat.server.session.LocalSession;
import io.netty.channel.Channel;


/**
 * @author : zxm
 * @date: 2024/6/5 - 15:40
 * @Description: com.example.douyin_chat.server.process
 * @version: 1.0
 */
public abstract class AbstractServerProcesser implements ServerReciever{

    protected String getKey(Channel ch){
        return ch.attr(LocalSession.KEY_USER_ID).get();
    }

    protected void setKey(Channel ch, String key){
        ch.attr(LocalSession.KEY_USER_ID).set(key);
    }

    protected void  checkAuth(Channel ch) throws Exception{
        if(null == getKey(ch)){
            throw new Exception("此用户没有登录成功");
        }
    }


}
