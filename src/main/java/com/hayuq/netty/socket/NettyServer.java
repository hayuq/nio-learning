package com.hayuq.netty.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NettyServer {

    private static final int DEFAULT_PORT = 8080;

    private int port;

    public NettyServer() {
        this.port = DEFAULT_PORT;
    }

    public NettyServer listen(int port) {
        this.port = port;
        return this;
    }

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new DiscardServerHandler(), new EchoServerHandler());
                    }

                })
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .bind(port).sync()
                .channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new NettyServer().start();
    }

    class DiscardServerHandler extends ChannelInboundHandlerAdapter {

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

    class EchoServerHandler extends ChannelInboundHandlerAdapter {

        private static final String CRLF = "\r\n";

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            StringBuilder builder = new StringBuilder(1000);
            builder.append("HTTP/1.1 200 OK" + CRLF);
            builder.append("Content-Type: text/html; charset=UTF-8" + CRLF);
            builder.append(CRLF);
            builder.append("<html>");
            builder.append("  <head>");
            builder.append("    <title>NIO Http Server</title>");
            builder.append("  </head>");
            builder.append("  <body>");
            builder.append("    <h1>Hello World!</h1>");
            builder.append("  </body>");
            builder.append("</html>");
            ctx.write(Unpooled.wrappedBuffer(builder.toString().getBytes()));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

    }

}
