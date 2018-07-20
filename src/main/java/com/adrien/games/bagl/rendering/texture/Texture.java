package com.adrien.games.bagl.rendering.texture;

import com.adrien.games.bagl.utils.Image;
import com.adrien.games.bagl.utils.ResourcePath;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

/**
 * OpenGL texture wrapper class
 *
 * @author adrien
 */
public final class Texture {

    private final int width;
    private final int height;
    private final TextureParameters parameters;
    private final int handle;

    /**
     * Create a new texture
     *
     * @param width      The width of the texture
     * @param height     The height of the texture
     * @param parameters The parameters of the texture
     */
    public Texture(final int width, final int height, final TextureParameters parameters) {
        this(width, height, ByteBuffer.class.cast(null), parameters);
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
    public Texture(final int width, final int height, final ByteBuffer pixels, final TextureParameters parameters) {
        this.width = width;
        this.height = height;
        this.parameters = parameters;
        this.handle = this.generateGlTexture(pixels);
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
    public Texture(final int width, final int height, final FloatBuffer pixels, final TextureParameters parameters) {
        this.width = width;
        this.height = height;
        this.parameters = parameters;
        this.handle = this.generateGlTexture(pixels);
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
    public static Texture fromFile(final ResourcePath path, final TextureParameters.Builder params) {
        return Texture.fromFile(path, false, params);
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
    public static Texture fromFile(final ResourcePath path, final boolean flipVertically, final TextureParameters.Builder params) {
        try (final var image = Image.fromFile(path, flipVertically)) {
            params.format(Texture.getFormat(image.getChannelCount(), image.isHdr()));
            return new Texture(image.getWidth(), image.getHeight(), image.getData(), params.build());
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
    public static Texture fromMemory(final ByteBuffer imageData, final TextureParameters.Builder params) {
        return Texture.fromMemory(imageData, false, params);
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
    public static Texture fromMemory(final ByteBuffer imageData, final boolean flipVertically, final TextureParameters.Builder params) {
        try (final var image = Image.fromMemory(imageData, flipVertically)) {
            params.format(Texture.getFormat(image.getChannelCount(), image.isHdr()));
            return new Texture(image.getWidth(), image.getHeight(), image.getData(), params.build());
        }
    }

    private int generateGlTexture(final ByteBuffer pixels) {
        final var handle = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, handle);
        glTexImage2D(GL_TEXTURE_2D, 0, this.parameters.getFormat().getGlInternalFormat(), this.width, this.height, 0,
                this.parameters.getFormat().getGlFormat(), this.parameters.getFormat().getGlDataType(), pixels);
        this.applyTextureParameters();
        glBindTexture(GL_TEXTURE_2D, 0);
        return handle;
    }

    private int generateGlTexture(final FloatBuffer pixels) {
        final var handle = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, handle);
        glTexImage2D(GL_TEXTURE_2D, 0, this.parameters.getFormat().getGlInternalFormat(), this.width, this.height, 0,
                this.parameters.getFormat().getGlFormat(), this.parameters.getFormat().getGlDataType(), pixels);
        this.applyTextureParameters();
        glBindTexture(GL_TEXTURE_2D, 0);
        return handle;
    }

    private void applyTextureParameters() {
        if (this.parameters.getMipmaps()) {
            glGenerateMipmap(GL_TEXTURE_2D);
        }
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, this.parameters.getMagFilter().getGlFilter());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, this.parameters.getMinFilter().getGlFilter());
        if (this.parameters.getAnisotropic() > 0) {
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, this.parameters.getAnisotropic());
        }
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, this.parameters.getsWrap().getGlWrap());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, this.parameters.gettWrap().getGlWrap());
    }

    private static Format getFormat(final int channelCount, final boolean isHdr) {
        switch (channelCount) {
            case 1:
                return isHdr ? Format.RED16F : Format.RED8;
            case 2:
                return isHdr ? Format.RG16F : Format.RG8;
            case 3:
                return isHdr ? Format.RGB16F : Format.RGB8;
            case 4:
                return isHdr ? Format.RGBA16F : Format.RGBA8;
            default:
                throw new IllegalArgumentException("A texture cannot be composed of less than 1 or more that 4 channels");
        }
    }

    /**
     * Release resources
     */
    public void destroy() {
        glDeleteTextures(handle);
    }

    /**
     * Bind the textures to the default texture unit (0)
     */
    public void bind() {
        this.bind(0);
    }

    /**
     * Binds the texture to the given texture unit
     *
     * @param unit The texture unit to bind the texture to
     */
    public void bind(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D, handle);
    }

    /**
     * Unbinds the texture bound to the default texture unit (0)
     */
    public static void unbind() {
        Texture.unbind(0);
    }

    /**
     * Unbinds the texture bound to the given texture unit
     *
     * @param unit The texture unit to unbind the texture from
     */
    public static void unbind(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D, 0);
    }


    public int getHandle() {
        return this.handle;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public TextureParameters getParameters() {
        return this.parameters;
    }
}

