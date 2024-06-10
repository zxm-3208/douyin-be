package com.example.douyin_chat_commons.protocol.codec;


import com.example.douyin_chat_commons.protocol.bean.ProtoMsgOuterClass;
import com.example.douyin_chat_commons.protocol.constant.ProtoInstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author : zxm
 * @date: 2024/5/31 - 14:58
 * @Description: 解码器
 * @version: 1.0
 */
@Slf4j
public class ProtobufDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 标记当前readIndex的位置（用于追踪缓冲区中当前读取位置,使用resetReaderIndex方法可以回到这个标记位置）
        byteBuf.markReaderIndex();
        // 判断包头长度 (short+short+int)
        if(byteBuf.readableBytes()<8){
            return;
        }
        // 读取魔数
        short magic = byteBuf.readShort();
        log.info("解码——魔数:{}", magic);
        if(magic != ProtoInstant.MAGIC_CODE){
            String error = "客户端口令不对:" + ctx.channel().remoteAddress();
        }
        // 读取版本
        short version = byteBuf.readShort();
        log.info("解码——版本:{}", version);
        // 读取传送过来的消息长度
        int length = byteBuf.readInt();

        if(length<0){
            ctx.close();
        }
        if(length>byteBuf.readableBytes()){
            // 重置读取位置
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] array;
        if(byteBuf.hasArray()){
            // 堆缓存
            ByteBuf slice = byteBuf.slice(byteBuf.readerIndex(), length);
            array = slice.array();
            byteBuf.retain();       // 可重用，增加引用次数，与release配合使用
        }else{
            // 直接缓存
            array = new byte[length];
            byteBuf.readBytes(array,0,length);  // 从byteBuf中读取并写到array中
        }
        // 字节转成对象
        ProtoMsgOuterClass.ProtoMsg.Message outmsg = ProtoMsgOuterClass.ProtoMsg.Message.parseFrom(array);
        if(byteBuf.hasArray()){
            byteBuf.release();
        }
        if(outmsg != null){
            // 获取业务消息
            list.add(outmsg);
        }
        log.info("解码——消息体:{}", outmsg);
    }
}
