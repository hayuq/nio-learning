package com.hayuq.nio.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 原生NIO实现的服务器
 * @author hayuq
 */
public class NioServer {
    
    private static final int DEFAULT_PORT = 8080; 
    
    private int port;
    
    public NioServer() {
        this.port = DEFAULT_PORT;
    }
    
    public NioServer listen(int port) {
        this.port = port;
        return this;
    }
    
    public void start() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        // 注册接受客户端连接事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        System.out.println("Server startup on port " + port + "......");
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
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    if (selectionKey.isReadable()) {
                        // 处理read事件
                        buffer.clear();
                        int readCount = socketChannel.read(buffer);
                        if (readCount > 0) {                            
                            System.out.println("收到来自客户端的消息: " + new String(buffer.array()));
                            // 注册write事件
                            selectionKey.interestOps(SelectionKey.OP_WRITE);
                        } else {
                            socketChannel.close();
                        }
                    } else if (selectionKey.isWritable()) {
                        // 处理write事件
                        buffer.rewind();
                        socketChannel.write(buffer);
                        socketChannel.close();
                    }
                }
                // 移除已经处理过的事件
                iterator.remove();
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        new NioServer().start();
    }

}