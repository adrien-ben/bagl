package com.adrien.games.bagl.utils;

import java.nio.FloatBuffer;

public class HDRImage {

    private final int width;
    private final int height;
    private final int channelCount;
    private final FloatBuffer data;

    public HDRImage(final int width, final int height, final int channelCount, final FloatBuffer data) {
        this.width = width;
        this.height = height;
        this.channelCount = channelCount;
        this.data = data;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getChannelCount() {
        return this.channelCount;
    }

    public FloatBuffer getData() {
        return this.data;
    }
}
