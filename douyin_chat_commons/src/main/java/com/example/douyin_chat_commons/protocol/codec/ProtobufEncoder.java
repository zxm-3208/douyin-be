package com.example.douyin_chat_commons.protocol.codec;


import com.example.douyin_chat_commons.protocol.bean.ProtoMsgOuterClass;
import com.example.douyin_chat_commons.protocol.constant.ProtoInstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : zxm
 * @date: 2024/5/31 - 14:59
 * @Description: 编码器
 * @version: 1.0
 */
@Slf4j
@ChannelHandler.Sharable
public class ProtobufEncoder extends MessageToByteEncoder<ProtoMsgOuterClass.ProtoMsg.Message> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ProtoMsgOuterClass.ProtoMsg.Message message, ByteBuf byteBuf) throws Exception {
        // 1. 魔数
        byteBuf.writeShort(ProtoInstant.MAGIC_CODE);
        // 2. 版本号
        byteBuf.writeShort(ProtoInstant.VERSION_CODE);

        byte[] bytes = message.toByteArray();
        int length = bytes.length;

        // 3. 写入消息长度(消息头)
        byteBuf.writeInt(length);
        // 4. 写入数据(消息体)
        byteBuf.writeBytes(bytes);
        log.info("编码——消息头:{}, 消息体:{}",length, bytes);


    }
}
