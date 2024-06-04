package com.example.douyin_chat.distributed;

import com.example.douyin_chat.entity.ChatUserDTO;
import com.example.douyin_chat.entity.ImNode;
import com.example.douyin_chat.protocol.bean.ProtoMsgOuterClass;
import com.example.douyin_chat.protocol.codec.ProtobufDecoder;
import com.example.douyin_chat.protocol.codec.ProtobufEncoder;
import com.example.douyin_chat.protocol.protoBuilder.NotificationMsgBuilder;
import com.example.douyin_chat.server.handler.ImNodeExceptionHandler;
import com.example.douyin_chat.server.handler.ImNodeHeartBeatClientHandler;
import com.example.douyin_chat.util.JsonUtil;
import com.example.douyin_chat.util.Notification;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author : zxm
 * @date: 2024/6/2 - 19:48
 * @Description: com.example.douyin_chat.distributed
 * @version: 1.0
 */
@Slf4j
@Data
public class PeerSender {

    private int reConnectCount = 0;
    private Channel channel;
    private ImNode rmNode;
    /**
     * 唯一标记
     */
    private boolean connectFlag = false;
    private ChatUserDTO user;

    private Bootstrap b;
    private EventLoopGroup g;

    GenericFutureListener<ChannelFuture> closeListener = (ChannelFuture f) ->{
        log.info("分布式连接已经断开...{}", rmNode.toString());
        channel = null;
        connectFlag = false;
    };

    // 判断是否连接成功，成功：发送通知；没成功：重连
    private GenericFutureListener<ChannelFuture> connectedListener = (ChannelFuture f)->{
        final EventLoop eventLoop = f.channel().eventLoop();
        if(!f.isSuccess()&&++reConnectCount<3){
            log.info("连接失败！在十秒之后准备尝试第{}次重连!", reConnectCount);
            eventLoop.schedule(()->PeerSender.this.doConnect(), 10, TimeUnit.SECONDS);      // 在匿名内部类中，this强调使用外部PeerSender类的方法
            connectFlag = false;
        }else{
            connectFlag = true;
            log.info(new Date() +"分布式节点连接成功:{}",rmNode.toString());

            channel = f.channel();
            channel.closeFuture().addListener(closeListener);
        }

        /**
         * 发送链接成功的通知
         */
        Notification<ImNode> notification = new Notification<>(ImWorker.getInst().getLocalNode());
        notification.setType(Notification.CONNECT_FINISHED);
        String json = JsonUtil.pojoToJson(notification);
        ProtoMsgOuterClass.ProtoMsg.Message pkg = NotificationMsgBuilder.buildNotification(json);   // 上面构造的是对象类，在这里转换成protobuf消息对象，且采用build模式
        writeAndFlush(pkg);
    };

    public PeerSender(ImNode n){
        this.rmNode = n;

        // 客户端使用Bootstrap引导器， 服务端使用ServerBootStrap引导器
        b = new Bootstrap();

        // 使用NIO方式来接收连接和处理连接
        g = new NioEventLoopGroup();
    }

    public void doConnect(){
        // 服务器ip地址
        String host = rmNode.getHost();
        // 服务器端口
        int port = rmNode.getPort();

        try{
            if(b != null && b.group() == null){
                // 1. 设置reactor线程
                b.group(g);
                // 2. 设置nio类型的channel
                b.channel(NioSocketChannel.class);
                // 3. 设置监听端口
                b.remoteAddress(host, port);
                // 4. 设置通道选项
                b.option(ChannelOption.SO_KEEPALIVE, true);     // 在网络连接空闲时自动发送“心跳”包（如ACK报文），以检测对端是否仍然在线
                b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);      // 缓冲区分配方式
                // 5. 责任链
                b.handler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                // TODO: 半包、黏包
                                ch.pipeline().addLast("decoder", new ProtobufDecoder());
                                ch.pipeline().addLast("encoder", new ProtobufEncoder());
                                ch.pipeline().addLast("imNodeHeartBeatClientHandler", new ImNodeHeartBeatClientHandler());
                                ch.pipeline().addLast("exceptionHandler", new ImNodeExceptionHandler());
                            }
                        }
                );
                log.info(new Date() + "开始连接分布式节点:{}", rmNode.toString());
                ChannelFuture f = b.connect();
                f.addListener(connectedListener);

                // 阻塞，直到通道关闭操作完成
                f.channel().closeFuture().sync();
            }else if(b.group() != null){
                log.info(new Date() + "再一次开始连接分布式节点", rmNode.toString());
                ChannelFuture f = b.connect();
                f.addListener(connectedListener);
            }
        } catch (Exception e) {
            log.info("客户端连接失败:{}", e.getMessage());
        }
    }

    public void writeAndFlush(Object pkg){
        if(connectFlag == false){
            log.error("分布式节点未连接:", rmNode.toString());
            return;
        }
        channel.writeAndFlush(pkg);
    }

    public void stopConnecting(){
        g.shutdownGracefully();     // 等任务提交后关闭
        connectFlag = false;
    }


}
