package com.hayuq.netty.custom;

import com.hayuq.netty.custom.codec.CustomMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class CustomMessageClient {

    private String host;
    private int port;

    public CustomMessageClient bind(String host, int port) {
        this.host = host;
        this.port = port;
        return this;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<Channel>() {

                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new CustomMessageEncoder())
                                    .addLast(new StringDecoder())
                                    .addLast(new ClientHandler());
                        }

                    })
                    .connect(host, port).sync()
                    .channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new CustomMessageClient().bind("localhost", 8090).start();
    }

    class ClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            byte[] content = "测试消息".getBytes();
            int length = content.length;
            ctx.writeAndFlush(new CustomMessage(length, content));
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("收到服务端消息：" + msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

    }


}
