package com.adrien.games.bagl.rendering;

import org.lwjgl.opengl.GL11;

/**
 * Color blending mode.
 */
public enum BlendMode {

    TRANSPARENCY(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA),
    ADDITIVE(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

    private final int glSource;
    private final int glDestination;

    BlendMode(int glSource, int glDestination) {
        this.glSource = glSource;
        this.glDestination = glDestination;
    }

    public int getGlSource() {
        return glSource;
    }

    public int getGlDestination() {
        return glDestination;
    }
}
