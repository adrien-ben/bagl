package com.adrien.games.bagl.utils;

import java.nio.ByteBuffer;

public class Image {

    private final int width;
    private final int height;
    private final int channelCount;
    private final ByteBuffer data;

    public Image(int width, int height, int channelCount, ByteBuffer data) {
        this.width = width;
        this.height = height;
        this.channelCount = channelCount;
        this.data = data;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public ByteBuffer getData() {
        return data;
    }

}
