package com.hayuq.nio;

import com.hayuq.nio.http.NioHttpServer;

import java.io.IOException;

public class NioTest {

    public static void main(String[] args) throws IOException {
        new NioHttpServer().start();
    }
    
}
