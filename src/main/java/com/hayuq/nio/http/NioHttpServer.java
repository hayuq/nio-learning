package com.hayuq.nio.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

/**
 * 原生NIO实现的HTTP服务器
 * @author hayuq
 */
public class NioHttpServer {
    
    private static final String CRLF = "\r\n";

    private static final int DEFAULT_PORT = 8080;
    
    private int port;
    
    private String uri;
    
    private String method;
    
    public NioHttpServer() {
        this.port = DEFAULT_PORT;
    }
    
    public NioHttpServer listen(int port) {
        this.port = port;
        return this;
    }
    
    public void start() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        Selector selector = Selector.open();
        // 注册接受客户端连接事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server listening on port " + port + "......");
        while (selector.select() > 0) {
            // Event Loop
            // 如果监听到事件，则进行处理
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            for (Iterator<SelectionKey> iterator = selectedKeys.iterator(); iterator.hasNext();) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    serverSocketChannel
                        // 接受客户端连接请求
                        .accept()
                        .configureBlocking(false)
                        // 注册read事件
                        .register(selector, SelectionKey.OP_READ);
                } else {
                    if (selectionKey.isReadable()) {
                        handleRequest(selectionKey);
                    } else if (selectionKey.isWritable()) {
                        handleResponse(selectionKey);
                    }
                }
                // 移除已经处理过的事件
                iterator.remove();
            }
        }
    }
    
    private void handleRequest(SelectionKey selectionKey) throws IOException {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.clear();
        boolean hasContent = false;
        if (channel.read(buffer) > 0) {
            String request = new String(buffer.array());
            System.out.println(request);
            String[] requestLines = request.split(CRLF);
            String[] firstLine = requestLines[0].split(" ");
            method = firstLine[0];
            uri = firstLine[1];
            hasContent = true;
        }
        if (hasContent) {
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        } else {
            channel.close();
        }
    }

    private void handleResponse(SelectionKey selectionKey) throws IOException {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        if (!Arrays.asList("HEAD", "GET", "POST").contains(method)) {
            handleMethodNotSopported(channel);
        } else if (Arrays.asList("/", "/index.html").contains(uri)) {
            handleIndexPage(channel);
        } else {
            handleNotFound(channel);
        }
    }
    
    private void handleIndexPage(SocketChannel channel) throws IOException {
        StringBuilder builder = new StringBuilder(1000);
        builder.append("HTTP/1.1 200 OK" + CRLF);
        builder.append("Content-Type: text/html; charset=UTF-8" + CRLF);
        builder.append(CRLF);
        builder.append("<html>");
        builder.append("  <head>");
        builder.append("    <title>NIO Http Server</title>");
        builder.append("  </head>");
        builder.append("  <body>");
        builder.append("    <h1>Hello there!</h1>");
        builder.append("  </body>");
        builder.append("</html>");
        write(channel, builder.toString());
    }

    private void write(SocketChannel channel, String content) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(content.getBytes());        
        buffer.rewind();
        channel.write(buffer);
        channel.close();
    }

    private void handleNotFound(SocketChannel channel) throws IOException {
        handleError(channel, "404 Not Found");
    }

    private void handleMethodNotSopported(SocketChannel channel) throws IOException {
        handleError(channel, "405 Method Not Alowed");
    }
    
    private void handleError(SocketChannel channel, String statusLine) throws IOException {
        StringBuilder builder = new StringBuilder(100);
        builder.append("HTTP/1.1 " + statusLine + CRLF);
        builder.append("Content-Type: text/html; charset=UTF-8" + CRLF);
        builder.append(CRLF);
        builder.append("<html>");
        builder.append("  <head>");
        builder.append("    <title>NIO Http Server</title>");
        builder.append("  </head>");
        builder.append("  <body>");
        builder.append("    <h1>" + statusLine + "</h1>");
        builder.append("  </body>");
        builder.append("</html>");
        write(channel, builder.toString());
    }

    public static void main(String[] args) throws IOException {
        new NioHttpServer().start();
    }

}