package com.hayuq.netty.custom;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomMessage {

    private int length;
    private byte[] content;

    @Override
    public String toString() {
        return "content: " + new String(content) + ", length: " + length;
    }

}
