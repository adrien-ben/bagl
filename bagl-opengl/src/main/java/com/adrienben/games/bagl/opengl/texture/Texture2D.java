package com.adrienben.games.bagl.opengl.texture;

import com.adrienben.games.bagl.core.Asset;
import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.core.utils.Image;
import com.adrienben.games.bagl.opengl.AccessMode;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glTexImage2D;

/**
 * OpenGL texture wrapper class
 *
 * @author adrien
 */
public final class Texture2D extends Texture implements Asset {

    private final int width;
    private final int height;

    /**
     * Create a new texture
     *
     * @param width      The width of the texture
     * @param height     The height of the texture
     * @param parameters The parameters of the texture
     */
    public Texture2D(final int width, final int height, final TextureParameters parameters) {
        this(width, height, (ByteBuffer) null, parameters);
    }

    /**
     * Creates a texture from a pixel byte buffer. You have to take extra care of
     * parameters consistency. If texture format is not consistent with the
     * content of pixel buffer and dimensions of the texture, errors will arise
     *
     * @param width      The width of the texture
     * @param height     The height of the texture
     * @param pixels     The pixel data (bytes)
     * @param parameters The texture parameter
     */
    public Texture2D(final int width, final int height, final ByteBuffer pixels, final TextureParameters parameters) {
        super(Type.TEXTURE_2D, parameters);
        this.width = width;
        this.height = height;
        generateGlTexture(pixels);
    }

    /**
     * Creates a texture from a pixel float buffer. You have to take extra care of
     * parameters consistency. If texture format is not consistent with the
     * content of pixel buffer and dimensions of the texture, errors will arise
     *
     * @param width      The width of the texture
     * @param height     The height of the texture
     * @param pixels     The pixel data (floats)
     * @param parameters The texture parameter
     */
    public Texture2D(final int width, final int height, final FloatBuffer pixels, final TextureParameters parameters) {
        super(Type.TEXTURE_2D, parameters);
        this.width = width;
        this.height = height;
        generateGlTexture(pixels);
    }

    /**
     * Create a texture from an image file
     * <p>
     * Here we expect the builder as parameters instead of the built parameters
     * because the texture format will be inferred from the image file format
     *
     * @param path   The path to the image file
     * @param params The parameters builder
     * @return A new texture
     */
    public static Texture2D fromFile(final ResourcePath path, final TextureParameters.Builder params) {
        return Texture2D.fromFile(path, false, params);
    }

    /**
     * Create a texture from an image file
     * <p>
     * Here we expect the builder as parameters instead of the built parameters
     * because the texture format will be inferred from the image file format
     *
     * @param path           The path to the image file
     * @param flipVertically Should the image be flipped vertically
     * @param params         The parameters builder
     * @return A new texture
     */
    public static Texture2D fromFile(final ResourcePath path, final boolean flipVertically, final TextureParameters.Builder params) {
        try (final var image = Image.fromFile(path, flipVertically)) {
            params.format(Texture2D.getFormat(image.getChannelCount(), image.isHdr()));
            return new Texture2D(image.getWidth(), image.getHeight(), image.getData(), params.build());
        }
    }

    /**
     * Create a texture from an image stored in memory
     * <p>
     * Here we expect the builder as parameters instead of the built parameters
     * because the texture format will be inferred from the image file format
     * <p>
     * {@code imageData} must contain an actual image (jpeg, png, tga...) not just the pixel data.
     *
     * @param imageData The buffer containing the image
     * @param params    The parameters builder
     * @return A new texture
     */
    public static Texture2D fromMemory(final ByteBuffer imageData, final TextureParameters.Builder params) {
        return Texture2D.fromMemory(imageData, false, params);
    }

    /**
     * Create a texture from an image stored in memory
     * <p>
     * Here we expect the builder as parameters instead of the built parameters
     * because the texture format will be inferred from the image file format
     * <p>
     * {@code imageData} must contain an actual image (jpeg, png, tga...) not just the pixel data.
     *
     * @param imageData      The buffer containing the image
     * @param flipVertically Should the image be flipped vertically
     * @param params         The parameters builder
     * @return A new texture
     */
    public static Texture2D fromMemory(final ByteBuffer imageData, final boolean flipVertically, final TextureParameters.Builder params) {
        try (final var image = Image.fromMemory(imageData, flipVertically)) {
            params.format(Texture2D.getFormat(image.getChannelCount(), image.isHdr()));
            return new Texture2D(image.getWidth(), image.getHeight(), image.getData(), params.build());
        }
    }

    private void generateGlTexture(final ByteBuffer pixels) {
        bind();
        final var parameters = getParameters();
        glTexImage2D(GL_TEXTURE_2D, 0, parameters.getFormat().getGlInternalFormat(), width, height, 0,
                parameters.getFormat().getGlFormat(), parameters.getFormat().getGlDataType(), pixels);
        applyTextureParameters();
        unbind();
    }

    private void generateGlTexture(final FloatBuffer pixels) {
        bind();
        final var parameters = getParameters();
        glTexImage2D(GL_TEXTURE_2D, 0, parameters.getFormat().getGlInternalFormat(), width, height, 0,
                parameters.getFormat().getGlFormat(), parameters.getFormat().getGlDataType(), pixels);
        applyTextureParameters();
        unbind();
    }

    private static Format getFormat(final int channelCount, final boolean isHdr) {
        return switch (channelCount) {
            case 1 -> isHdr ? Format.RED16F : Format.RED8;
            case 2 -> isHdr ? Format.RG16F : Format.RG8;
            case 3 -> isHdr ? Format.RGB16F : Format.RGB8;
            case 4 -> isHdr ? Format.RGBA16F : Format.RGBA8;
            default -> throw new IllegalArgumentException("A texture cannot be composed of less than 1 or more that 4 channels");
        };
    }

    private void applyTextureParameters() {
        applyMipmapParameter();
        applyMinFilterParameters();
        applyMagFilterParameters();
        applyAnisotropicParameter();
        applySWrapParameter();
        applyTWrapParameter();
        applyBorderColorParameter();
        applyCompareFunctionParameters();
    }

    /**
     * Bind this texture as an image texture.
     * <p>
     * Note that not every texture format is compatible with image texture binding.
     *
     * @param imageUnit  The image unit to which to bind the image texture.
     * @param level      The level of the texture to bind.
     * @param accessMode The access mode of the image texture.
     */
    public void bindAsImageTexture(final int imageUnit, final int level, final AccessMode accessMode) {
        bindAsImageTexture(imageUnit, level, false, 0, accessMode);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}

