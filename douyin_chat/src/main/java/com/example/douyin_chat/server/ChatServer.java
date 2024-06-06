package com.example.douyin_chat.server;

import com.example.douyin_chat.cocurrent.FutureTaskScheduler;
import com.example.douyin_chat.distributed.ImWorker;
import com.example.douyin_chat.distributed.WorkerRouter;
import com.example.douyin_chat.protocol.codec.ProtobufDecoder;
import com.example.douyin_chat.protocol.codec.ProtobufEncoder;
import com.example.douyin_chat.server.handler.ChatRedirectHandler;
import com.example.douyin_chat.server.handler.LoginRequestHandler;
import com.example.douyin_chat.server.handler.RemoteNotificationHandler;
import com.example.douyin_chat.server.handler.ServerExceptionHandler;
import com.example.douyin_chat.util.IOUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.rmi.ServerException;
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
    @Autowired
    private RemoteNotificationHandler remoteNotificationHandler;
    @Autowired
    private ChatRedirectHandler chatRedirectHandler;
    @Autowired
    private ServerExceptionHandler serverExceptionHandler;

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
                ch.pipeline().addLast("login", loginRequestHandler);        // 成功登录后login处理器转换成心跳处理器
                ch.pipeline().addLast("remoteNotificationHandler", remoteNotificationHandler);  // 远程节点通知(上线，下线，链接)
                ch.pipeline().addLast("chatRedirect", chatRedirectHandler);     // 消息接收，转发
                ch.pipeline().addLast("serverException", serverExceptionHandler);
            }
        });
        // 6. 绑定Server
        // 调用sync同步方法阻塞 直到绑定成功
        ChannelFuture channelFuture = null;
        boolean isStart = false;
        while(!isStart){
            try{
                channelFuture = serverBootstrap.bind().sync();
                log.info("IM启动，端口为:{}", channelFuture.channel().localAddress());
                isStart = true;
            } catch (InterruptedException e) {
                log.error("发生启动异常:{}", e);
                port++;
                log.info(" 尝试一个新的端口:{}", port);
                serverBootstrap.localAddress(new InetSocketAddress(port));
            }
        }

        ImWorker.getInst().setLocalNode(ip, port);

        FutureTaskScheduler.add(()->{
            // 启动节点
            ImWorker.getInst().init();
            // 启动节点的管理
            WorkerRouter.getInstance().init();
        });

        // JVM 关闭时的钩子函数
        Runtime.getRuntime().addShutdownHook(
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 8. 关闭EventLoopGroup
                        // 释放所有资源包括创建的线程
                        WorkerGroup.shutdownGracefully();
                        BossGroup.shutdownGracefully();
                    }
                })
        );
        // 7. 监听通道关闭事件
        try{
            // 应用会一直等待，直到channel关闭
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();
            closeFuture.sync();
        } catch (InterruptedException e) {
            log.error("发生其他异常", e);
        } finally {
            // 8 优雅关闭EventLoopGroup，
            // 释放掉所有资源包括创建的线程
            WorkerGroup.shutdownGracefully();
            BossGroup.shutdownGracefully();
        }

    }


}
