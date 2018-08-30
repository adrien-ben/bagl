package com.adrienben.games.bagl.opengl;

import com.adrienben.games.bagl.core.Color;
import com.adrienben.games.bagl.opengl.texture.Texture;
import com.adrienben.games.bagl.opengl.texture.Type;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
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
    private static final FloatBuffer COLOR_PARAM_BUFFER = MemoryUtil.memAllocFloat(4);

    private OpenGL() {
    }

    /**
     * Bind a texture on a texture unit.
     *
     * @param texture     The texture to bind.
     * @param textureUnit The texture unit to use.
     * @throws IllegalArgumentException if the texture unit is already used by another texture.
     */
    public static void bindTexture(final Texture texture, final int textureUnit) {
        if (Objects.nonNull(BOUND_TEXTURES[textureUnit])) {
            throw new IllegalArgumentException("You cannot bind several textures to the same texture unit");
        }
        glActiveTexture(GL_TEXTURE0 + textureUnit);
        glBindTexture(texture.getType().getGlCode(), texture.getHandle());
        BOUND_TEXTURES[textureUnit] = texture;
    }

    /**
     * Unbind a texture from a texture unit.
     *
     * @param texture     The texture to bind.
     * @param textureUnit The texture unit to use.
     * @throws IllegalArgumentException if the texture is not bound on the given texture unit.
     */
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
     * Set a texture parameter on the currently bound texture.
     *
     * @param type           The type of the texture.
     * @param parameterCode  The code of the parameter.
     * @param parameterValue The value of the parameter.
     */
    public static void setParameterI(final Type type, final int parameterCode, final int parameterValue) {
        glTexParameteri(type.getGlCode(), parameterCode, parameterValue);
    }

    /**
     * Set a texture parameter on the currently bound texture.
     *
     * @param type           The type of the texture.
     * @param parameterCode  The code of the parameter.
     * @param parameterValue The value of the parameter.
     */
    public static void setParameterF(final Type type, final int parameterCode, final float parameterValue) {
        glTexParameterf(type.getGlCode(), parameterCode, parameterValue);
    }

    /**
     * Set a texture parameter on the currently bound texture.
     *
     * @param type          The type of the texture.
     * @param parameterCode The code of the parameter.
     * @param color         The value of the parameter.
     */
    public static void setParameterColor(final Type type, final int parameterCode, final Color color) {
        COLOR_PARAM_BUFFER.put(0, color.getRed());
        COLOR_PARAM_BUFFER.put(1, color.getGreen());
        COLOR_PARAM_BUFFER.put(2, color.getBlue());
        COLOR_PARAM_BUFFER.put(3, color.getAlpha());
        glTexParameterfv(type.getGlCode(), parameterCode, COLOR_PARAM_BUFFER);
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

    /**
     * Enable writing to the depth buffer.
     */
    public static void enableDepthWrite() {
        glDepthMask(true);
    }

    /**
     * Disable writing to the depth buffer.
     */
    public static void disableDepthWrite() {
        glDepthMask(false);
    }
}
