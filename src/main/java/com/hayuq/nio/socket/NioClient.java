package com.hayuq.nio.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class NioClient {

    private int port;
    
    public NioClient bind(int port) {
        this.port = port;
        return this;
    }
    
    public void start() throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(port));
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Scanner in = new Scanner(System.in);
        System.out.println("请输入要发送的内容：");
        String line = in.nextLine();
        buffer.rewind();
        buffer.put(line.getBytes(StandardCharsets.UTF_8));
        socketChannel.write(buffer);
        buffer.clear();
        socketChannel.read(buffer);
        System.out.println("收到服务器的响应: " + new String(buffer.array()));
        in.close();
    }
    
    public static void main(String[] args) throws IOException {
        new NioClient().bind(8080).start();
    }

}
