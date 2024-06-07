package com.example.douyin_chat.client.client;

import com.example.douyin_chat.client.handler.ChatMsgHandler;
import com.example.douyin_chat.client.handler.ExceptionHandler;
import com.example.douyin_chat.client.handler.LoginResponceHandler;
import com.example.douyin_chat.client.sender.LoginSender;
import com.example.douyin_chat.entity.ChatUserDTO;
import com.example.douyin_chat.protocol.codec.ProtobufDecoder;
import com.example.douyin_chat.protocol.codec.ProtobufEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author : zxm
 * @date: 2024/6/7 - 11:46
 * @Description: com.example.douyin_chat.client
 * @version: 1.0
 */
@Slf4j
@Data
@Service
public class NettyClient {

    // 服务器IP地址
    private String host;
    // 服务器端口
    private int port;

    @Autowired
    private LoginResponceHandler loginResponseHandler;

    @Autowired
    private ChatMsgHandler chatMsgHandler;

    @Autowired
    private ExceptionHandler exceptionHandler;

//    private Channel channel;
//    private ChatSender sender;
//    private LoginSender loginSender;

    /**
     * 唯一标记
     */
    private boolean initFlag = true;
    private ChatUserDTO user;
    private GenericFutureListener<ChannelFuture> connectedListener;

    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;

    /**
     * 重连
     */
    public void doConnect(){
        try{
            // 启动器
            bootstrap = new Bootstrap();
            // 监听线程组
            eventLoopGroup = new NioEventLoopGroup();
            // 1. 设置reactor线程
            bootstrap.group(eventLoopGroup);
            // 2. 设置nio类型的channel
            bootstrap.channel(NioSocketChannel.class);
            // 3. 设置监听端口
            bootstrap.remoteAddress(host, port);
            // 4. 设置通道选项
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            // 5. 责任链
            bootstrap.handler(
                    new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("decoder", new ProtobufDecoder());
                            ch.pipeline().addLast("encode", new ProtobufEncoder());
                            ch.pipeline().addLast("loginResponseHandler", loginResponseHandler);
                            ch.pipeline().addLast("chatMsgHandler", chatMsgHandler);
                            ch.pipeline().addLast("exceptionHandler", exceptionHandler);
                        }
                    }
            );
            log.info("客户端开始连接");
            ChannelFuture f = bootstrap.connect();
            f.addListener(connectedListener);

        } catch (Exception e) {
            log.info("客户端连接失败:{}", e.getMessage());
        }
    }

    public void close(){
        getEventLoopGroup().shutdownGracefully();
    }

}
