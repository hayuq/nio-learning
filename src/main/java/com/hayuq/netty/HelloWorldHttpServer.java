package com.hayuq.netty;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.nio.charset.StandardCharsets;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


public class HelloWorldHttpServer {
    
    private static final int DEFAULT_PORT = 8080; 
    
    private int port;
    
    public HelloWorldHttpServer() {
        this.port = DEFAULT_PORT;
    }
    
    public HelloWorldHttpServer listen(int port) {
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
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                            .addLast(new HttpServerCodec())
                            .addLast(new HttpServerExpectContinueHandler())
                            .addLast(new HelloWorldServerHandler());
                    }
                    
                })
                .option(ChannelOption.SO_BACKLOG, 100)
                .childOption(ChannelOption.SO_KEEPALIVE, true).bind(port).sync()
                .channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        new HelloWorldHttpServer().start();
        System.out.println("Server listening on port 8080......");
    }
    
    static class HelloWorldServerHandler extends SimpleChannelInboundHandler<HttpRequest> {
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpRequest request) throws Exception {
            boolean keepAlive = HttpUtil.isKeepAlive(request);
            StringBuilder builder = new StringBuilder(100);
            builder.append("<html>");
            builder.append("  <head>");
            builder.append("    <title>NIO Http Server</title>");
            builder.append("  </head>");
            builder.append("  <body>");
            builder.append("    <h1>Hello World!</h1>");
            builder.append("  </body>");
            builder.append("</html>");
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(builder.toString(), StandardCharsets.UTF_8));
            response.headers()
                .set(CONTENT_TYPE, "text/html")
                .setInt(CONTENT_LENGTH, response.content().readableBytes());

            if (keepAlive && request.protocolVersion().equals(HttpVersion.HTTP_1_0)) {
                response.headers().set(CONNECTION, KEEP_ALIVE);
            } else {
                response.headers().set(CONNECTION, CLOSE);
            }

            ChannelFuture future = ctx.writeAndFlush(response);
            if (!keepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

    }
}



