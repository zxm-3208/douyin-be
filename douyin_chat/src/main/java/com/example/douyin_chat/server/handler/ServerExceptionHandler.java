package com.example.douyin_chat.server.handler;

import com.example.douyin_chat.exception.InvalidFrameException;
import com.example.douyin_chat.server.session.service.SessionManger;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author : zxm
 * @date: 2024/6/6 - 15:15
 * @Description: com.example.douyin_chat.server.handler
 * @version: 1.0
 */
@Slf4j
@Service
@ChannelHandler.Sharable
public class ServerExceptionHandler extends ChannelInboundHandlerAdapter {

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
        if (cause instanceof InvalidFrameException)
        {
            log.error(cause.getMessage());

        } else
        {

            //捕捉异常信息
            cause.printStackTrace();
            log.error(cause.getMessage());
        }

        SessionManger.inst().closeSession(ctx);
        ctx.close();
    }

    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
    {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx)
            throws Exception
    {
        SessionManger.inst().closeSession(ctx);

    }

}
