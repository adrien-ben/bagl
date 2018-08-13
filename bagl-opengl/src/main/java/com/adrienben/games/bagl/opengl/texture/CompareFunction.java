package com.adrienben.games.bagl.opengl.texture;

import static org.lwjgl.opengl.GL11.*;

/**
 * Texture comparison function.
 * <p>
 * Contains the possible values for the texture parameter GL_TEXTURE_COMPARE_FUNC.
 *
 * @author adrien
 */
public enum CompareFunction {

    NONE(-1),
    LEQUAL(GL_LEQUAL),
    GEQUAL(GL_GEQUAL),
    LESS(GL_LESS),
    GREATER(GL_GREATER),
    EQUAL(GL_EQUAL),
    NOTEQUAL(GL_NOTEQUAL),
    ALWAYS(GL_ALWAYS),
    NEVER(GL_NEVER);

    private final int glCode;

    CompareFunction(final int glCode) {
        this.glCode = glCode;
    }

    public int getGlCode() {
        return glCode;
    }
}
