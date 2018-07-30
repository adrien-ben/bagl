package com.adrien.games.bagl.opengl;

import static org.lwjgl.opengl.GL15.*;

/**
 * Usage of OpenGL buffer.
 */
public enum BufferUsage {

    STATIC_DRAW(GL_STATIC_DRAW),
    STATIC_READ(GL_STATIC_READ),
    STATIC_COPY(GL_STATIC_COPY),
    STREAM_DRAW(GL_STREAM_DRAW),
    STREAM_READ(GL_STREAM_READ),
    STREAM_COPY(GL_STREAM_COPY),
    DYNAMIC_DRAW(GL_DYNAMIC_DRAW),
    DYNAMIC_READ(GL_DYNAMIC_READ),
    DYNAMIC_COPY(GL_DYNAMIC_COPY);

    final private int glCode;

    BufferUsage(int glCode) {
        this.glCode = glCode;
    }

    public int getGlCode() {
        return glCode;
    }

}
