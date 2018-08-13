package com.adrienben.games.bagl.opengl;

import com.adrienben.games.bagl.core.Color;
import com.adrienben.games.bagl.opengl.texture.Texture;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

/**
 * OpenGL utility.
 *
 * @author adrien
 */
public final class OpenGL {

    private static final int TEXTURE_UNITS_COUNT = 1024;
    private static final Texture[] BOUND_TEXTURES = new Texture[TEXTURE_UNITS_COUNT];

    private OpenGL() {
    }

    public static void bindTexture(final Texture texture, final int textureUnit) {
        if (Objects.nonNull(BOUND_TEXTURES[textureUnit])) {
            throw new IllegalArgumentException("You cannot bind several textures to the same texture unit");
        }
        glActiveTexture(GL_TEXTURE0 + textureUnit);
        glBindTexture(texture.getType().getGlCode(), texture.getHandle());
        BOUND_TEXTURES[textureUnit] = texture;
    }

    public static void unbindTexture(final Texture texture, final int textureUnit) {
        final var boundTexture = BOUND_TEXTURES[textureUnit];
        if (!texture.equals(boundTexture)) {
            throw new IllegalArgumentException(String.format("You cannot unbind texture %d from unit %d since it is not bound", texture.getHandle(), textureUnit));
        }
        glActiveTexture(GL_TEXTURE0 + textureUnit);
        glBindTexture(texture.getType().getGlCode(), 0);
        BOUND_TEXTURES[textureUnit] = null;
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
