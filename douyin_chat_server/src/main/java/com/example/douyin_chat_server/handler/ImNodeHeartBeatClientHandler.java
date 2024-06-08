package com.example.douyin_chat_server.handler;


import com.example.douyin_chat_server.distributed.ImWorker;
import com.example.douyin_chat_commons.protocol.bean.ProtoMsgOuterClass;
import com.example.douyin_chat_commons.util.JsonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author : zxm
 * @date: 2024/6/4 - 14:48
 * @Description: com.example.douyin_chat.server.handler
 * @version: 1.0
 */
@Slf4j
@ChannelHandler.Sharable
public class ImNodeHeartBeatClientHandler extends ChannelInboundHandlerAdapter {
    String from = null;
    int seq = 0;
    // 心跳的时间间隔,单位为s
    private static final int HEARBEAT_INTERVAL = 50;

    public ProtoMsgOuterClass.ProtoMsg.Message buildMessageHeartBeat(){
        if(null == from){
            from = JsonUtil.pojoToJson(ImWorker.getInst().getLocalNode());
        }

        ProtoMsgOuterClass.ProtoMsg.Message.Builder mb = ProtoMsgOuterClass.ProtoMsg.Message.newBuilder()
                .setType(ProtoMsgOuterClass.ProtoMsg.HeadType.HEART_BEAT)
                .setSequence(++seq);
        ProtoMsgOuterClass.ProtoMsg.MessageHeartBeat.Builder heartBeat =
                ProtoMsgOuterClass.ProtoMsg.MessageHeartBeat.newBuilder()
                        .setSeq(seq)
                        .setJson(from)
                        .setUid("-1");
        mb.setHeartBeat(heartBeat.build());
        return mb.build();
    }

    // 当Handler被加入到pipeline时，开始发送心跳
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // 发送心跳
        heartBeat(ctx);
    }

    // 当有新的数据从网络读取到，并准备好被应用层处理时 (入站)
    // 接收到服务器的心跳回写
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(null == msg || !(msg instanceof ProtoMsgOuterClass.ProtoMsg.Message)){
            super.channelRead(ctx, msg);
            return;
        }
        // 判断类型
        ProtoMsgOuterClass.ProtoMsg.Message pkg = (ProtoMsgOuterClass.ProtoMsg.Message) msg;
        ProtoMsgOuterClass.ProtoMsg.HeadType headType = pkg.getType();
        if(headType.equals(ProtoMsgOuterClass.ProtoMsg.HeadType.HEART_BEAT)){
            ProtoMsgOuterClass.ProtoMsg.MessageHeartBeat heartBeat = pkg.getHeartBeat();
            log.info("收到imNode Heart_beat 消息 from:{}", heartBeat.getJson());
            log.info("收到imNode Heart_beat seq:{}", heartBeat.getSeq());
        }else{
            super.channelRead(ctx, msg);
        }
    }

    // 使用定时器，发送心跳报文
    public void heartBeat(ChannelHandlerContext ctx){
        ProtoMsgOuterClass.ProtoMsg.Message message = buildMessageHeartBeat();

        ctx.executor().schedule(()->{
            if(ctx.channel().isActive()){
                log.info("发送ImNode Heart_beat 消息");
                ctx.writeAndFlush(message);

                // 递归调用发送下一次的心跳
                heartBeat(ctx);
            }
        },HEARBEAT_INTERVAL, TimeUnit.SECONDS);
    }
}
