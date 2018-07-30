package com.adrien.games.bagl.opengl;

import org.lwjgl.opengl.GL11;

/**
 * Color blending mode.
 */
public enum BlendMode {

    NONE(Constants.NONE_VALUE, Constants.NONE_VALUE),
    DEFAULT(GL11.GL_ONE, GL11.GL_ZERO),
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

    private static class Constants {
        private static final int NONE_VALUE = -1;
    }

}