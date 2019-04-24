package com.hayuq.nio;

import java.io.IOException;

public class NioTest {

    public static void main(String[] args) throws IOException {
        new NioHttpServer().start();
    }
    
}
