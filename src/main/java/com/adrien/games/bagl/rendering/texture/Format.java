package com.adrien.games.bagl.rendering.texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public enum Format {

    RGBA8(GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE),
    RGBA16(GL_RGBA16, GL_RGBA, GL_UNSIGNED_BYTE),
    RGBA16F(GL_RGBA16F, GL_RGBA, GL_FLOAT),
    RGBA32F(GL_RGBA32F, GL_RGBA, GL_FLOAT),
    RGB8(GL_RGB8, GL_RGB, GL_UNSIGNED_BYTE),
    RGB16F(GL_RGB16F, GL_RGB, GL_FLOAT),
    RED8(GL_R8, GL_RED, GL_UNSIGNED_BYTE),
    ALPHA8(GL_ALPHA8, GL_ALPHA, GL_UNSIGNED_BYTE),
    DEPTH_32F(GL_DEPTH_COMPONENT32F, GL_DEPTH_COMPONENT, GL_FLOAT);

    private final int glInternalFormat;
    private final int glFormat;
    private final int glDataType;

    Format(int glInternalFormat, int glFormat, int glDataType) {
        this.glInternalFormat = glInternalFormat;
        this.glFormat = glFormat;
        this.glDataType = glDataType;

    }

    public int getGlInternalFormat() {
        return this.glInternalFormat;
    }

    public int getGlFormat() {
        return this.glFormat;
    }

    public int getGlDataType() {
        return this.glDataType;
    }

}
