package com.adrienben.games.bagl.opengl;

import org.lwjgl.opengl.GL11;

/**
 * Mesh primitives
 *
 * @author adrien
 */
public enum PrimitiveType {

    POINTS(GL11.GL_POINTS),
    TRIANGLES(GL11.GL_TRIANGLES),
    TRIANGLE_STRIP(GL11.GL_TRIANGLE_STRIP);

    private final int glCode;

    PrimitiveType(final int glCode) {
        this.glCode = glCode;
    }

    public int getGlCode() {
        return this.glCode;
    }
}
