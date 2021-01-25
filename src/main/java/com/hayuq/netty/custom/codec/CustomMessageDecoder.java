package com.hayuq.netty.custom.codec;

import com.hayuq.netty.custom.CustomMessage;
import com.hayuq.netty.custom.MessageConstants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class CustomMessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magic = in.readInt();
        byte version = in.readByte();
        if (magic != MessageConstants.MAGIC || version != MessageConstants.VERSION) {
            log.warn("消息格式错误, magic: {}, version: {}", magic, version);
            return;
        }
        int length = in.readInt();
        int bytes = in.readableBytes();
        if (bytes != length) {
            log.warn("内容长度不匹配 length：{}，actual：{}", length, bytes);
            return;
        }
        byte[] data = new byte[length];
        in.readBytes(data);
        out.add(new CustomMessage(length, data));
    }

}
