package com.example.douyin_chat.client.handler;

import com.example.douyin_chat.client.service.ClientService;
import com.example.douyin_chat.exception.BusinessException;
import com.example.douyin_chat.exception.InvalidFrameException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author : zxm
 * @date: 2024/6/7 - 14:41
 * @Description: com.example.douyin_chat.client.handler
 * @version: 1.0
 */
@Service
@Slf4j
@ChannelHandler.Sharable
public class ExceptionHandler extends ChannelInboundHandlerAdapter {

//    @Autowired
//    private ClientService clientService;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cause instanceof BusinessException){
            // 业务异常
            log.error(cause.getMessage());
        }else if(cause instanceof InvalidFrameException){
            // 报文异常
            log.error(cause.getMessage());
        }else{
            log.error(cause.getMessage());
            ctx.close();

//            //开始重连
//            clientService.setConnectFlag(false);
//            clientService.startConnectServer();
        }
    }

    /**
     * 通道 Read 读取 Complete 完成
     * 做刷新操作 ctx.flush()
     */
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


}
