package com.adrienben.games.bagl.opengl;

import static org.lwjgl.opengl.GL15.*;

/**
 * Memory access mode.
 *
 * @author adrien
 */
public enum AccessMode {

    READ_ONLY(GL_READ_ONLY),
    WRITE_ONLY(GL_WRITE_ONLY),
    READ_WRITE(GL_READ_WRITE);

    private final int glCode;

    AccessMode(final int glCode) {
        this.glCode = glCode;
    }

    public int getGlCode() {
        return glCode;
    }
}
