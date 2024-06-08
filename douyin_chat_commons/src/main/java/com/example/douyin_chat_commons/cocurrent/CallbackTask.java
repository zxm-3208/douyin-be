package com.example.douyin_chat_commons.cocurrent;

/**
 * @author : zxm
 * @date: 2024/6/5 - 21:02
 * @Description: com.example.douyin_chat.cocurrent
 * @version: 1.0
 */
public interface CallbackTask<R>
{

    R execute() throws Exception;

    void onBack(R r);

    void onException(Throwable t);
}
