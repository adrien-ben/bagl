package com.adrienben.games.bagl.opengl.buffer;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL21.GL_PIXEL_PACK_BUFFER;
import static org.lwjgl.opengl.GL21.GL_PIXEL_UNPACK_BUFFER;
import static org.lwjgl.opengl.GL30.GL_TRANSFORM_FEEDBACK_BUFFER;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL42.GL_ATOMIC_COUNTER_BUFFER;
import static org.lwjgl.opengl.GL43.GL_DISPATCH_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL44.GL_QUERY_BUFFER;

/**
 * OpenGL buffer target.
 *
 * @author adrien
 */
public enum BufferTarget {

    ARRAY(GL_ARRAY_BUFFER),
    ELEMENT_ARRAY(GL_ELEMENT_ARRAY_BUFFER),
    COPY_READ(GL_COPY_READ_BUFFER),
    COPY_WRITE(GL_COPY_WRITE_BUFFER),
    PIXEL_PACK(GL_PIXEL_PACK_BUFFER),
    PIXEL_UNPACK(GL_PIXEL_UNPACK_BUFFER),
    QUERY(GL_QUERY_BUFFER),
    TEXTURE(GL_TEXTURE_BUFFER),
    TRANSFORM_FEEDBACK(GL_TRANSFORM_FEEDBACK_BUFFER),
    UNIFORM(GL_UNIFORM_BUFFER),
    DRAW_INDIRECT(GL_DRAW_INDIRECT_BUFFER),
    ATOMIC_COUNTER(GL_ATOMIC_COUNTER_BUFFER),
    DISPATCH_INDIRECT(GL_DISPATCH_INDIRECT_BUFFER),
    SHADER_STORAGE(GL_SHADER_STORAGE_BUFFER);

    private final int glCode;

    BufferTarget(final int glCode) {
        this.glCode = glCode;
    }

    public int getGlCode() {
        return glCode;
    }
}
