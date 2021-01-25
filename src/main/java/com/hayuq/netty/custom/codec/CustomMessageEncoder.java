package com.hayuq.netty.custom.codec;

import com.hayuq.netty.custom.CustomMessage;
import com.hayuq.netty.custom.MessageConstants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class CustomMessageEncoder extends MessageToByteEncoder<CustomMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, CustomMessage msg, ByteBuf out) throws Exception {
        out.writeInt(MessageConstants.MAGIC);
        out.writeByte(MessageConstants.VERSION);
        out.writeInt(msg.getLength());
        out.writeBytes(msg.getContent());
    }

}
