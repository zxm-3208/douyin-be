package com.example.douyin_chat.server;

import com.example.douyin_chat.protocol.codec.ProtobufDecoder;
import com.example.douyin_chat.protocol.codec.ProtobufEncoder;
import com.example.douyin_chat.server.handler.LoginRequestHandler;
import com.example.douyin_chat.util.IOUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.EventListener;

/**
 * @author : zxm
 * @date: 2024/5/31 - 17:45
 * @Description: com.example.douyin_chat.server
 * @version: 1.0
 */
@Service
@Data
@Slf4j
public class ChatServer {

    // 服务器端口
    @Value("${server.port}")
    private int port;

    // 通过NIO方式来接收和处理连接
    private EventLoopGroup BossGroup;       // 监听线程(主Reactor)
    private EventLoopGroup WorkerGroup;     // 传输处理线程(从Reactor)

    @Autowired
    private LoginRequestHandler loginRequestHandler;

    public void run(){
        // 启动引导器
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        // 连接监听线程组
        BossGroup = new NioEventLoopGroup(1);
        WorkerGroup = new NioEventLoopGroup();      // 默认是0
        // 1. 设置reactor线程
        serverBootstrap.group(BossGroup, WorkerGroup);
        // 2. 设置nio类型的channel
        serverBootstrap.channel(NioServerSocketChannel.class);
        // 3. 设置监听端口
        String ip = IOUtil.getHostAddress();
        serverBootstrap.localAddress(new InetSocketAddress(ip, port));
        // 4. 设置通道选项
        serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        serverBootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        // 5. 责任链
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 管理pipeline中的Handler
                // TODO: 半包、黏包
                ch.pipeline().addLast("deCoder", new ProtobufDecoder());
                ch.pipeline().addLast("enCoder", new ProtobufEncoder());
                // 在流水线中添加handler来处理登录，登录后删除
                ch.pipeline().addLast("login", l)
            }
        });


    }


}
