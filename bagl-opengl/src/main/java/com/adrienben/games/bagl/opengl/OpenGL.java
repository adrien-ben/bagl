package com.adrienben.games.bagl.opengl;

import com.adrienben.games.bagl.core.Color;
import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.opengl.shader.compute.Barrier;
import com.adrienben.games.bagl.opengl.texture.Texture;
import com.adrienben.games.bagl.opengl.texture.Type;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.EnumSet;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL42.GL_ALL_BARRIER_BITS;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

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
     * Perform a 1D compute dispatch.
     *
     * @param x The number of work groups to start in the x dimension
     */
    public static void dispatchCompute(final int x) {
        glDispatchCompute(x, 1, 1);
    }

    /**
     * Perform a 2D compute dispatch.
     *
     * @param x The number of work groups to start in the x dimension
     * @param y The number of work groups to start in the y dimension
     */
    public static void dispatchCompute(final int x, final int y) {
        glDispatchCompute(x, y, 1);
    }

    /**
     * Perform a 3D compute dispatch.
     *
     * @param x The number of work groups to start in the x dimension
     * @param y The number of work groups to start in the y dimension
     * @param z The number of work groups to start in the z dimension
     */
    public static void dispatchCompute(final int x, final int y, final int z) {
        glDispatchCompute(x, y, z);
    }

    /**
     * Set up memory barriers.
     *
     * @param barriers The barriers to set up.
     */
    public static void setMemoryBarriers(final EnumSet<Barrier> barriers) {
        if (CollectionUtils.isNotEmpty(barriers)) {
            final int bitField = barriers.stream().mapToInt(Barrier::getGlCode).reduce(0, (a, b) -> a | b);
            glMemoryBarrier(bitField);
        }
    }

    /**
     * Set up all memory barriers
     */
    public static void setAllMemoryBarriers() {
        glMemoryBarrier(GL_ALL_BARRIER_BITS);
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
