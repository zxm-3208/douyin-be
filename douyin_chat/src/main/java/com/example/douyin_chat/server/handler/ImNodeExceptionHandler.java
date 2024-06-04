package com.example.douyin_chat.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : zxm
 * @date: 2024/6/4 - 16:31
 * @Description: com.example.douyin_chat.server.handler
 * @version: 1.0
 */
@Slf4j
@ChannelHandler.Sharable
public class ImNodeExceptionHandler extends ChannelInboundHandlerAdapter {

    // channel 读取完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    // channel 异常回调
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 捕捉异常信息
        cause.printStackTrace();
        log.error(cause.getMessage());
        ctx.close();
    }
}
