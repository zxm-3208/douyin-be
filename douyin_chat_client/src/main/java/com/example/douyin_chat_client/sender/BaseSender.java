package com.example.douyin_chat_client.sender;


import com.example.douyin_chat_client.client.ClientSession;
import com.example.douyin_chat_commons.cocurrent.CallbackTask;
import com.example.douyin_chat_commons.cocurrent.CallbackTaskScheduler;
import com.example.douyin_chat_commons.entity.ChatUserDTO;
import com.example.douyin_chat_commons.protocol.bean.ProtoMsgOuterClass;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author : zxm
 * @date: 2024/6/7 - 11:50
 * @Description: com.example.douyin_chat.client.sender
 * @version: 1.0
 */
@Data
@Slf4j
public abstract class BaseSender {
    private ChatUserDTO user;
    private ClientSession session;

    public boolean isConnected(){
        if(null == session){
            log.info("session is null");
            return false;
        }
        return session.isConnected();
    }

    public boolean isLogin()
    {
        if (null == session)
        {
            log.info("session is null");
            return false;
        }

        return session.isLogin();
    }

    public void sendMsg(ProtoMsgOuterClass.ProtoMsg.Message message){
        CallbackTaskScheduler.add(new CallbackTask<Boolean>() {

            @Override
            public Boolean execute() throws Exception {
                if(null == getSession()){
                    throw new Exception("session is null");
                }
                if(!isConnected()){
                    log.info("连接还没成功");
                    throw new Exception("连接还没成功");
                }
                final Boolean[] isSuccess = {false};
                ChannelFuture f = getSession().writeAndFlush(message);
                f.addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        // 回调
                        if(future.isSuccess()){
                            isSuccess[0] = true;
                            log.info("操作成功");
                        }
                    }
                });
                try{
                    f.sync();
                } catch (InterruptedException e) {
                    isSuccess[0] = false;
                    e.printStackTrace();
                    throw new Exception("error occur");
                }
                return isSuccess[0];
            }

            @Override
            public void onBack(Boolean aBoolean) {
                if(aBoolean){
                    BaseSender.this.sendSucced(message);        // this 保证调用的是外部类的实例；如果不加this则会调用匿名内部类下的实例
                }
                else{
                    BaseSender.this.sendfailed(message);
                }
            }

            @Override
            public void onException(Throwable t) {
                BaseSender.this.sendException(message);
            }
        });
    }



    protected void sendSucced(ProtoMsgOuterClass.ProtoMsg.Message message)
    {
        log.info("发送成功");

    }

    protected void sendfailed(ProtoMsgOuterClass.ProtoMsg.Message message)
    {
        log.info("发送失败");
    }

    protected void sendException(ProtoMsgOuterClass.ProtoMsg.Message message)
    {
        log.info("发送消息出现异常");

    }

}
