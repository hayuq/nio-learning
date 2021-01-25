package com.hayuq.netty.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {

    private static final int DEFAULT_PORT = 8080;

    private int port;

    public NettyClient() {
        this.port = DEFAULT_PORT;
    }

    public NettyClient bind(int port) {
        this.port = port;
        return this;
    }

    public void start() throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {            
            new Bootstrap()
            .group(workerGroup)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .handler(new ChannelInitializer<Channel>() {
                
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new ClientHandler());
                }
                
            })
            .connect("localhost", port).sync()
            .channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new NettyClient().start();
    }

    class ClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            try {
                while (buf.isReadable()) {
                    System.out.print((char) buf.readByte());
                    System.out.flush();
                }
            } finally {
                buf.release();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

    }

}
