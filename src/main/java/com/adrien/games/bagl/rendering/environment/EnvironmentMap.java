package com.adrien.games.bagl.rendering.environment;

import com.adrien.games.bagl.rendering.texture.Cubemap;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_BYTE;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.*;

/**
 * Environment map
 *
 * @author adrien
 */
public class EnvironmentMap {

    private final static byte SKYBOX_POSITIVE_HALF_SIZE = (byte) 1;
    private final static byte SKYBOX_NEGATIVE_HALF_SIZE = (byte) -1;

    private final int vboId;
    private final int vaoId;
    private final int iboId;
    private final Cubemap cubemap;

    public EnvironmentMap(final Cubemap cubemap) {
        this.vaoId = glGenVertexArrays();
        this.vboId = glGenBuffers();
        this.generateVertexBuffer();
        this.iboId = this.generateIndexBuffer();
        this.cubemap = cubemap;
    }

    private int generateIndexBuffer() {
        final int iboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboId);
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final ByteBuffer indices = stack.bytes(
                    (byte) 1, (byte) 0, (byte) 3, (byte) 3, (byte) 0, (byte) 2,
                    (byte) 5, (byte) 1, (byte) 7, (byte) 7, (byte) 1, (byte) 3,
                    (byte) 4, (byte) 5, (byte) 6, (byte) 6, (byte) 5, (byte) 7,
                    (byte) 0, (byte) 4, (byte) 2, (byte) 2, (byte) 4, (byte) 6,
                    (byte) 6, (byte) 7, (byte) 2, (byte) 2, (byte) 7, (byte) 3,
                    (byte) 0, (byte) 1, (byte) 4, (byte) 4, (byte) 1, (byte) 5
            );
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        }
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        return iboId;
    }

    private void generateVertexBuffer() {
        glBindVertexArray(this.vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, this.vboId);
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final ByteBuffer vertices = stack.bytes(
                    SKYBOX_NEGATIVE_HALF_SIZE, SKYBOX_NEGATIVE_HALF_SIZE, SKYBOX_POSITIVE_HALF_SIZE,
                    SKYBOX_POSITIVE_HALF_SIZE, SKYBOX_NEGATIVE_HALF_SIZE, SKYBOX_POSITIVE_HALF_SIZE,
                    SKYBOX_NEGATIVE_HALF_SIZE, SKYBOX_POSITIVE_HALF_SIZE, SKYBOX_POSITIVE_HALF_SIZE,
                    SKYBOX_POSITIVE_HALF_SIZE, SKYBOX_POSITIVE_HALF_SIZE, SKYBOX_POSITIVE_HALF_SIZE,
                    SKYBOX_NEGATIVE_HALF_SIZE, SKYBOX_NEGATIVE_HALF_SIZE, SKYBOX_NEGATIVE_HALF_SIZE,
                    SKYBOX_POSITIVE_HALF_SIZE, SKYBOX_NEGATIVE_HALF_SIZE, SKYBOX_NEGATIVE_HALF_SIZE,
                    SKYBOX_NEGATIVE_HALF_SIZE, SKYBOX_POSITIVE_HALF_SIZE, SKYBOX_NEGATIVE_HALF_SIZE,
                    SKYBOX_POSITIVE_HALF_SIZE, SKYBOX_POSITIVE_HALF_SIZE, SKYBOX_NEGATIVE_HALF_SIZE);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        }
        glEnableVertexAttribArray(0);
        glVertexAttribIPointer(0, 3, GL_BYTE, 3, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void destroy() {
        glDeleteBuffers(this.iboId);
        glDeleteBuffers(this.vboId);
        glDeleteVertexArrays(this.vaoId);
        this.cubemap.destroy();
    }

    public int getVaoId() {
        return this.vaoId;
    }

    public int getIboId() {
        return this.iboId;
    }

    public Cubemap getCubemap() {
        return cubemap;
    }
}
