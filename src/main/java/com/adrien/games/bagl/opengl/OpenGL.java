package com.adrien.games.bagl.opengl;

import com.adrien.games.bagl.core.Color;

import static org.lwjgl.opengl.GL11.*;

/**
 * OpenGL utility.
 *
 * @author adrien
 */
public final class OpenGL {

    private OpenGL() {
    }

    /**
     * Sets the color to une when clear the color buffer. This color is only applied to the currently
     * bound framebuffer. The engine must have been started first.
     *
     * @param color The clear color.
     */
    public static void setClearColor(final Color color) {
        glClearColor(color.getRed(), color.getGreen(), color.getBlue(), 1);
    }

    /**
     * Sets the blend mode for the current rendering context.
     *
     * @param blendMode The blend mode to apply.
     */
    public static void setBlendMode(final BlendMode blendMode) {
        if (blendMode == BlendMode.NONE) {
            glDisable(GL_BLEND);
        } else {
            glEnable(GL_BLEND);
            glBlendFunc(blendMode.getGlSource(), blendMode.getGlDestination());
        }
    }
}
